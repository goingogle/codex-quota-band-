package com.codex.quota.android.runtime

import android.content.Context
import com.codex.quota.android.BuildConfig
import com.codex.quota.android.domain.SyncedTask
import com.codex.quota.android.notifications.TaskNotificationContent
import com.xiaomi.xms.wearable.Wearable
import com.xiaomi.xms.wearable.auth.Permission
import com.xiaomi.xms.wearable.message.OnMessageReceivedListener
import com.xiaomi.xms.wearable.node.DataItem
import com.xiaomi.xms.wearable.node.OnDataChangedListener
import com.xiaomi.xms.wearable.node.Node
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Direct Android -> Xiaomi Vela bridge. AstroBox is deliberately not involved here.
 * The bridge only sends the already-filtered quota snapshot and never accepts control data.
 */
class XiaomiWearableBridge(
  context: Context,
  private val repository: RuntimeStateRepository,
) {
  private val appContext = context.applicationContext
  private val nodeApi = Wearable.getNodeApi(appContext)
  private val authApi = Wearable.getAuthApi(appContext)
  private val messageApi = Wearable.getMessageApi(appContext)
  private val notifyApi = Wearable.getNotifyApi(appContext)
  private var activeNode: Node? = null
  private var messageListenerRegistered = false
  private var connectionSubscriptionRegistered = false

  fun start() {
    refresh()
  }

  fun requestPermission(onResult: (Boolean) -> Unit = {}) {
    val node = activeNode
    if (node == null) {
      nodeApi
        .connectedNodes
        .addOnSuccessListener { nodes ->
          val discoveredNode = nodes.firstOrNull()
          activeNode = discoveredNode
          if (discoveredNode == null) {
            onResult(false)
            repository.setBandConnected(false)
            return@addOnSuccessListener
          }
          requestPermissionForNode(discoveredNode, onResult)
        }
        .addOnFailureListener {
          onResult(false)
          repository.setBandConnected(false)
        }
      return
    }
    requestPermissionForNode(node, onResult)
  }

  private fun requestPermissionForNode(node: Node, onResult: (Boolean) -> Unit) {
    authApi
      .requestPermission(node.id, Permission.DEVICE_MANAGER, Permission.NOTIFY)
      .addOnSuccessListener {
        onResult(true)
        refresh()
      }
      .addOnFailureListener {
        onResult(false)
      }
  }

  fun refresh() {
    nodeApi
      .connectedNodes
      .addOnSuccessListener { nodes ->
        val node = nodes.firstOrNull()
        activeNode = node
        if (node == null) {
          repository.setBandConnected(false)
          return@addOnSuccessListener
        }
        nodeApi.isWearAppInstalled(node.id).addOnSuccessListener { installed ->
          if (!installed) {
            repository.setBandConnected(false)
            return@addOnSuccessListener
          }
          authApi
            .checkPermissions(node.id, REQUIRED_PERMISSIONS)
            .addOnSuccessListener { granted ->
              if (granted.size < REQUIRED_PERMISSIONS.size || granted.any { !it }) {
                repository.setBandConnected(false)
                return@addOnSuccessListener
              }
              nodeApi.query(node.id, DataItem.ITEM_CONNECTION).addOnSuccessListener { state ->
                if (state.isConnected) {
                  registerListeners(node)
                } else {
                  repository.setBandConnected(false)
                }
              }.addOnFailureListener { repository.setBandConnected(false) }
            }
            .addOnFailureListener { repository.setBandConnected(false) }
        }.addOnFailureListener { repository.setBandConnected(false) }
      }
      .addOnFailureListener {
        activeNode = null
        repository.setBandConnected(false)
      }
  }

  fun stop() {
    val node = activeNode ?: return
    if (messageListenerRegistered) {
      messageApi.removeListener(node.id)
      messageListenerRegistered = false
    }
    if (connectionSubscriptionRegistered) {
      nodeApi.unsubscribe(node.id, DataItem.ITEM_CONNECTION)
      connectionSubscriptionRegistered = false
    }
    repository.setBandConnected(false)
    activeNode = null
  }

  fun sendTaskAlert(task: SyncedTask): Boolean {
    val node = activeNode
    val content = TaskNotificationContent.from(task)
    if (node == null || content == null) return false
    return runCatching {
        notifyApi.sendNotify(node.id, content.title, content.body)
        true
      }
      .getOrDefault(false)
  }

  private fun registerListeners(node: Node) {
    if (!messageListenerRegistered) {
      messageApi
        .addListener(node.id, messageListener)
        .addOnSuccessListener { messageListenerRegistered = true }
    }
    if (!connectionSubscriptionRegistered) {
      nodeApi
        .subscribe(node.id, DataItem.ITEM_CONNECTION, connectionListener)
        .addOnSuccessListener { connectionSubscriptionRegistered = true }
    }
    repository.setBandConnected(true)
  }

  private fun handleIncoming(bytes: ByteArray) {
    val root = runCatching { Json.parseToJsonElement(bytes.toString(Charsets.UTF_8)).jsonObject }.getOrNull()
      ?: return
    if (root["type"]?.jsonPrimitive?.content != "quota_request") return
    val nonce = root["nonce"]?.jsonPrimitive?.content ?: return
    if (nonce.isBlank() || nonce.length > 64) return
    val node = activeNode ?: return
    val snapshot =
      repository.latestQuotaSnapshot()?.let {
        if (BuildConfig.DEMO_FIVE_HOUR_QUOTA) it.withDemoFiveHourQuota() else it
      }
    val taskSnapshot = buildBandTaskSnapshot(repository.latestTaskSnapshot())
    val payload =
      if (snapshot == null) {
        buildJsonObject {
          put("type", JsonPrimitive("quota_error"))
          put("nonce", JsonPrimitive(nonce))
          put("code", JsonPrimitive("quota_unavailable"))
          put("taskSnapshot", taskSnapshot)
        }
      } else {
        buildJsonObject {
          put("type", JsonPrimitive("quota_snapshot"))
          put("nonce", JsonPrimitive(nonce))
          put("snapshot", buildBandQuotaSnapshot(snapshot))
          put("taskSnapshot", taskSnapshot)
        }
      }
    messageApi.sendMessage(node.id, payload.toString().toByteArray(Charsets.UTF_8))
  }

  private val messageListener = OnMessageReceivedListener { _, bytes -> handleIncoming(bytes) }

  private val connectionListener = OnDataChangedListener { _, _, result ->
    repository.setBandConnected(result.connectedStatus == 1)
  }

  private companion object {
    val REQUIRED_PERMISSIONS = arrayOf(Permission.DEVICE_MANAGER, Permission.NOTIFY)
  }
}
