package com.awsm_guys.mobile_clicker.mobile

import io.reactivex.FlowableEmitter
import io.reactivex.FlowableOnSubscribe
import io.reactivex.disposables.Disposables
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean


class OnSubscribeUdpBroadcast(private var port: Int): FlowableOnSubscribe<DatagramPacket> {
    private val emitters = ConcurrentLinkedQueue<FlowableEmitter<DatagramPacket>>()

    private val socket by lazy {
        DatagramSocket(port).apply {
            broadcast = true
        }
    }

    private val isListening = AtomicBoolean(false)
    private val lock = Any()

    override fun subscribe(emitter: FlowableEmitter<DatagramPacket> ) {
        synchronized(lock, {
            if (!emitter.isCancelled) {
                emitters.add(emitter)
                emitter.setDisposable(Disposables.fromAction {
                    emitters.remove(emitter)
                    if (emitters.isEmpty()){
                        isListening.set(false)
                        socket.close()
                    }
                })
            }
        })

        if (!isListening.get()){
            startListening()
        }
    }

    private fun startListening() {
        isListening.set(true)
        val receiveData = ByteArray(1024)
        val datagramPacket = DatagramPacket(receiveData, receiveData.size)
        println("start listening")
        while (isListening.get()) {
            socket.receive(datagramPacket)
            println(String(datagramPacket.data))
            for(emitter in emitters){
                emitter.onNext(datagramPacket)
            }
        }
    }
}