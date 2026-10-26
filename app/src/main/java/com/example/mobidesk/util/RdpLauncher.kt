package com.example.mobidesk.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Utility for building and launching native Android Remote Desktop (RDP) URI intents.
 * Scheme format: rdp://full%20address=s:<IP>:<PORT>&username=s:<USERNAME>
 */
object RdpLauncher {

    const val DEFAULT_RDP_IP = "172.16.33.137"
    const val DEFAULT_RDP_PORT = 3389

    /**
     * Generates standard `rdp://` URI string format.
     * Example: rdp://full%20address=s:172.16.33.137:3389
     */
    fun buildRdpUriString(
        ipAddress: String?,
        port: Int = DEFAULT_RDP_PORT,
        username: String? = null
    ): String {
        val targetIp = ipAddress?.trim()?.ifBlank { null } ?: DEFAULT_RDP_IP
        val baseParam = "full%20address=s:$targetIp:$port"
        return if (!username.isNullOrBlank()) {
            "rdp://$baseParam&username=s:${username.trim()}"
        } else {
            "rdp://$baseParam"
        }
    }

    /**
     * Constructs a parsed [Uri] for rdp:// scheme.
     */
    fun buildRdpUri(
        ipAddress: String?,
        port: Int = DEFAULT_RDP_PORT,
        username: String? = null
    ): Uri {
        return Uri.parse(buildRdpUriString(ipAddress, port, username))
    }

    /**
     * Dispatches an [Intent.ACTION_VIEW] intent with the rdp:// URI scheme to launch native Android Remote Desktop apps.
     * If no RDP client application is installed on the device, shows fallback notice and opens Google Play Store.
     */
    fun launchRdpSession(
        context: Context,
        ipAddress: String?,
        port: Int = DEFAULT_RDP_PORT,
        username: String? = null,
        onNoClientInstalled: (() -> Unit)? = null
    ): Boolean {
        val uri = buildRdpUri(ipAddress, port, username)
        val rdpIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(rdpIntent)
            true
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "No RDP client found. Redirecting to Microsoft Remote Desktop on Google Play...",
                Toast.LENGTH_LONG
            ).show()

            onNoClientInstalled?.invoke()

            redirectToPlayStore(context)
            false
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to launch RDP session: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }

    /**
     * Opens Play Store / Browser for Microsoft Remote Desktop app download.
     */
    private fun redirectToPlayStore(context: Context) {
        val packageName = "com.microsoft.rdc.androidx"
        try {
            val marketIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packageName")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
        } catch (_: Exception) {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }
}
