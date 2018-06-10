package com.awsm_guys.mobile_clicker.mobile.udp

import com.awsm_guys.mobile_clicker.mobile.MobileClicker
import com.awsm_guys.mobile_clicker.mobile.MobileConnectionListener
import com.awsm_guys.mobile_clicker.mobile.udp.poko.ClickerMessage
import com.awsm_guys.mobile_clicker.mobile.udp.poko.Header
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class UdpMobileConnectionListener: MobileConnectionListener {

    private var broadcastPort = 8841
    private val onSubscribeUdpBroadcast by lazy { OnSubscribeUdpListener(broadcastPort) }
    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    override fun stopListening() {
        onSubscribeUdpBroadcast.stop()
    }

    override fun startListening(): Observable<MobileClicker> =
        Flowable.create(onSubscribeUdpBroadcast, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .map {
                    println("to message")
                    objectMapper.readValue(String(it.data), ClickerMessage::class.java)
                            .apply {
                                features["address"] = it.address.hostAddress
                            }
                }
                .retry()
                .filter{ it.header == Header.CONNECT }
                .map {
                    println("to clicker")
                    UdpMobileClicker(
                            it.features["address"]!!, it.features["port"]?.toInt()!!, it.body
                    ) as MobileClicker
                }
                .retry()
                .distinctUntilChanged()
                .toObservable()
}