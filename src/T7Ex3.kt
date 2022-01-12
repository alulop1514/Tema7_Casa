import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.DocumentChange
import com.google.cloud.storage.Blob
import com.google.cloud.storage.Bucket
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import com.google.firebase.cloud.StorageClient
import java.awt.BorderLayout
import java.awt.EventQueue
import java.awt.FlowLayout
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.nio.ByteBuffer
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.swing.*

class EstadisticaCF : JFrame() {

    private val etCombo = JLabel("Llista de Audios:")
    private val comboProv = JComboBox<String>()
    private val botonPlay = JButton("Play")
    private val botonStop = JButton("Stop")
    private val llistaNoms = arrayListOf<String>()
    var blob : Blob? = null
    val clip = AudioSystem.getClip()
    var audioSystem : AudioInputStream? = null

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        setBounds(100, 100, 450, 150)
        layout = BorderLayout()
        // contenidor per als elements

        val panell1 = JPanel(FlowLayout())
        panell1.add(etCombo)
        panell1.add(comboProv)
        contentPane.add(panell1, BorderLayout.NORTH)

        val panell2 = JPanel(FlowLayout())
        var bucket: Bucket?
        panell2.add(botonPlay)
        panell2.add(botonStop)
        contentPane.add(panell2, BorderLayout.SOUTH)

        isVisible = true

        val serviceAccount = FileInputStream("xat-ad-firebase-adminsdk-my2d0-8c69944b34.json")

        val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

        FirebaseApp.initializeApp(options)

        val database = FirestoreClient.getFirestore()
        bucket = StorageClient.getInstance().bucket("xat-ad.appspot.com")

        val collection = database.collection("Audios")
        collection.addSnapshotListener { snapshots, e ->
            if (e != null) {
                System.err.println("Listen failed: " + e)
                return@addSnapshotListener
            }
            for (dc in snapshots!!.getDocumentChanges()) {
                when (dc.getType()) {
                    DocumentChange.Type.ADDED -> {
                        comboProv.addItem(dc.getDocument().getString("nom"))
                        llistaNoms.add(dc.getDocument().getString("fitxer")!!)
                    }
                    DocumentChange.Type.MODIFIED ->
                        println("Missatge modificat: " + dc.getDocument().getData())

                    DocumentChange.Type.REMOVED ->
                        println("Missatge esborrat: " + dc.getDocument().getData())
                }
            }

        }

        // Instruccions per agafar la informació de tots els anys de la província triada

        comboProv.addActionListener {
            if (llistaNoms.size != 0) {
                blob = bucket.get(llistaNoms[comboProv.selectedIndex])
                val im = ByteBuffer.allocate(1024 * 1024)
                blob?.reader()?.read(im)
                audioSystem = AudioSystem.getAudioInputStream(ByteArrayInputStream(im.array()))
                clip.close()
                clip.open(audioSystem)
            }
        }
        botonPlay.addActionListener {
            clip.start()
        }
        botonStop.addActionListener {
            clip.stop()
        }
    }
}

fun main() {
    EventQueue.invokeLater {
        EstadisticaCF().isVisible = true
    }
}