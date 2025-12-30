
package com.example.nfcall

import android.app.PendingIntent
import android.content.Intent
import android.nfc.*
import android.nfc.tech.*
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent
    private lateinit var logView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logView = findViewById(R.id.logView)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        log("Ready. Select action then tap NFC tag.")
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
        dumpUID(tag)
        dumpTech(tag)
        dumpUltralight(tag)
        dumpMifareClassic(tag)
    }

    private fun dumpUID(tag: Tag) {
        val uid = tag.id.joinToString(" ") { "%02X".format(it) }
        log("UID: $uid")
    }

    private fun dumpTech(tag: Tag) {
        tag.techList.forEach { log("Tech: $it") }
    }

    private fun dumpUltralight(tag: Tag) {
        val ul = MifareUltralight.get(tag) ?: return
        ul.connect()
        for (p in 0..20) {
            log("Page $p: ${hex(ul.readPages(p))}")
        }
        ul.close()
    }

    private fun dumpMifareClassic(tag: Tag) {
        val mfc = MifareClassic.get(tag) ?: return
        mfc.connect()
        for (s in 0 until mfc.sectorCount) {
            if (mfc.authenticateSectorWithKeyA(s, MifareClassic.KEY_DEFAULT)) {
                val b = mfc.sectorToBlock(s)
                for (i in 0 until mfc.getBlockCountInSector(s)) {
                    log("Sector $s Block ${b+i}: ${hex(mfc.readBlock(b+i))}")
                }
            }
        }
        mfc.close()
    }

    private fun hex(b: ByteArray) = b.joinToString(" ") { "%02X".format(it) }
    private fun log(s: String) { logView.append(s + "\n") }
}
