package com.awsm_guys.mobile_clicker.presentation.viewinteractor.web.handlers

import com.awsm_guys.mobile_clicker.presentation.viewinteractor.web.WebSocketListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionDisconnectEvent


@Component
class StompDisconnectService: ApplicationListener<SessionDisconnectEvent> {

    @Autowired
    private lateinit var webSocketListener: WebSocketListener

    override fun onApplicationEvent(event: SessionDisconnectEvent) {
        webSocketListener.onDisconnected(event)
    }
}

