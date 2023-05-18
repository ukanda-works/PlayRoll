package es.ukanda.playroll.singleton

import es.ukanda.playroll.customExceptions.CustomException
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SocketSingleton constructor (private var serverIp: String, private var serverPort: Int) {

    lateinit var writer: BufferedWriter
    lateinit var reader: BufferedReader
    lateinit var clientSocket: Socket

    fun startClient() {
        try {
            clientSocket = Socket(serverIp, serverPort)
            println("Connected to server: $serverIp:$serverPort")

            writer = BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream()))
            reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))

        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    fun sendmensaje(message: String) {
        writer.write(message)
        writer.newLine()
        writer.flush()
        println("Sent message: $message")
    }

    fun escuchar(): String{
        val response = reader.readLine()
        println("Received response: $response")
        return response
    }

    fun close(){
        writer.close()
        reader.close()
        clientSocket.close()
    }
}

/*
class SocketSingleton private constructor(){
    private var socket: Socket = Socket()

    lateinit var ip: InetAddress

    fun connect(ip: InetAddress, timeout: Int) {
        this.ip = ip
        val executor = Executors.newSingleThreadExecutor()
        val future = executor.submit {
            socket = Socket()
            socket.connect(InetSocketAddress(ip, 5690), timeout)
        }

        try {
            future.get(timeout.toLong(), TimeUnit.MILLISECONDS)
            println("connected to $ip")
        } catch (e: Exception) {
            // Manejar la excepción de conexión aquí
            throw e
        } finally {
            executor.shutdownNow()
        }
    }


    fun send(data: String) {
        socket.getOutputStream().use { outputStream ->
            outputStream.write(data.toByteArray())
        }
    }

    fun close() {
        socket?.let {
            if (it.isConnected && !it.isClosed) {
                it.close()
            }
        }
    }

    fun recive(): String {
        if (!isSocketOpen()) {
            connect(ip?:throw CustomException("ip no asignada"), 5000)
        }
        val buffer = ByteArray(1024)
        val bytes = socket.getInputStream().use { inputStream ->
            inputStream.read(buffer)
        }
        return String(buffer, 0, bytes)
    }


    fun isSocketOpen(): Boolean {
        return socket?.isConnected == true && !socket?.isClosed!!
    }

    companion object {
        private val instance = SocketSingleton()

        fun getInstance(): SocketSingleton {
            if (!instance.isSocketOpen()) {
                    throw Exception("Socket not connected")
            }
            return instance
        }

        fun connect(ip: InetAddress, timeout: Int) {
            instance.connect(ip, timeout)
        }

    }
}*/