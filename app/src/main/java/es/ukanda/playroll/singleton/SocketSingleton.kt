package es.ukanda.playroll.singleton

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket


class SocketSingleton private constructor(){
    private lateinit var socket: Socket

    fun connect(ip: InetAddress) {
        socket = Socket()
        socket.connect(InetSocketAddress(ip, 5690), 5000)
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
                throw Exception("Socket is not open")
            }
            return instance
        }

        fun connect(ip: InetAddress) {
            instance.connect(ip)
        }
    }
}