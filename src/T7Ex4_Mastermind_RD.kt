import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import java.awt.*
import java.io.FileInputStream
import javax.swing.*

class Tirada(var nom: String, var tirada: String, var colocades: Int, var desordenades: Int)

class T7Ex4_Mastermind_RD : JFrame() {

    val etJugador = JLabel("Nom Jugador: ")
    val jugador = JTextField(15)

    val etiqueta = JLabel("Tirades:")
    val area = JTextArea()

    val etIntroduccioTirada = JLabel("Introdueix tirada:")
    val enviar = JButton("Enviar")
    val tirada = JTextField(15)

    val botoPartidaNova = JButton("PARTIDA NOVA")

    var numSecret = ""
    var finalitzada = false

    // en iniciar posem un contenidor per als elements anteriors
    init {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        setBounds(100, 100, 450, 300)
        setLayout(BorderLayout())

        // contenidor per als elements
        //Hi haurà títol. Panell de dalt: últim missatge. Panell de baix: per a introduir missatge. Panell central: tot el xat

        val panell1 = JPanel(FlowLayout())
        panell1.add(etJugador)
        panell1.add(jugador)
        getContentPane().add(panell1, BorderLayout.NORTH)

        val panell2 = JPanel(BorderLayout())
        panell2.add(etiqueta, BorderLayout.NORTH)
        area.setForeground(Color.BLUE)
        area.isEditable=false
        val scroll = JScrollPane(area)
        panell2.add(scroll, BorderLayout.CENTER)
        getContentPane().add(panell2, BorderLayout.CENTER)

        val panell3 = JPanel(FlowLayout())
        panell3.add(etIntroduccioTirada)
        panell3.add(tirada)
        panell3.add(enviar)
        getContentPane().add(panell3, BorderLayout.SOUTH)
        botoPartidaNova.isVisible=false
        panell3.add(botoPartidaNova)

        setVisible(true)
        enviar.addActionListener { enviar() }

        botoPartidaNova.addActionListener { partidaNova() }

        val serviceAccount = FileInputStream("xat-ad-firebase-adminsdk-my2d0-8c69944b34.json")

        val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://xat-ad.firebaseio.com/").build()

        FirebaseApp.initializeApp(options)

        title="MASTERMIND"

        // Exemple de listener de lectura única addListenerForSingleValue()
        // Per a AGAFAR SI ESTÀ LA PARTIDA EN MARXA I EL NÚMERO SECRET
        val mastermind = FirebaseDatabase.getInstance().getReference("Mastermind")

        mastermind.addListenerForSingleValueEvent(object : ValueEventListener {
            override
            fun onDataChange(dataSnapshot: DataSnapshot) {
                println("Entrem")
                if (dataSnapshot.child("finalitzada").getValue(Boolean::class.java)){
                    println("Patida nova")
                }
                else {
                    numSecret = dataSnapshot.child("numSecret").getValue(String::class.java)
                    finalitzada = dataSnapshot.child("finalitzada").getValue(Boolean::class.java)
                }
            }

            override
            fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun partidaNova(){
        area.text = ""
        val numSecret = FirebaseDatabase.getInstance().getReference("Mastermind").child("numSecret")
        numSecret.setValue(genera(), null)
        val finalitzada = FirebaseDatabase.getInstance().getReference("Mastermind").child("finalitzada")
        finalitzada.setValue(false, null)
        this.finalitzada = false
        val tirades = FirebaseDatabase.getInstance().getReference("Mastermind").child("tirades")
        tirades.removeValue(null)
        botoPartidaNova.isVisible = false
        enviar.isVisible = true
    }


    fun enviar(){
        if (!finalitzada) {
            val tirada = Tirada(
                jugador.text,
                tirada.text,
                comprova(tirada.text, numSecret)[0],
                comprova(tirada.text, numSecret)[1]
            )
            area.append("${tirada.nom}: ${tirada.tirada} (${tirada.colocades}, ${tirada.desordenades})\n")
            val tirades = FirebaseDatabase.getInstance().getReference("Mastermind").child("tirades")
            tirades.push().setValue(tirada, null)
            if (tirada.tirada == numSecret) {
                botoPartidaNova.isVisible = true
                enviar.isVisible = false
                val finalitzada = FirebaseDatabase.getInstance().getReference("Mastermind").child("finalitzada")
                finalitzada.setValue(true, null)
                this.finalitzada = true
            }
        }
        println(numSecret)

    }

    fun genera(): String {
        val i0 = (Math.random() * 10).toInt()
        var i1 = (Math.random() * 10).toInt()
        while (i1 == i0)
            i1 = (Math.random() * 10).toInt()
        var i2 = (Math.random() * 10).toInt()
        while (i2 == i0 || i2 == i1)
            i2 = (Math.random() * 10).toInt()
        var i3 = (Math.random() * 10).toInt()
        while (i3 == i0 || i3 == i1 || i3 == i2)
            i3 = (Math.random() * 10).toInt()
        return (i0.toString() + i1.toString() + i2.toString() + i3.toString())
    }

    fun comprova(num: String, sec: String): IntArray {
        var pos = 0
        var nopos = 0
        for (i in 0..3)
            for (j in 0..3)
                if (num[i] == sec[j])
                    if (i == j) pos++
                    else nopos++
        return intArrayOf(pos, nopos)
    }

}



fun main(args: Array<String>) {
    EventQueue.invokeLater {
        T7Ex4_Mastermind_RD().isVisible = true
    }
}