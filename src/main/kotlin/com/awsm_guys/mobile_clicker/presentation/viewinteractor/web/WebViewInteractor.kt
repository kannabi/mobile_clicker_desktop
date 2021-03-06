package com.awsm_guys.mobile_clicker.presentation.viewinteractor.web

import com.awsm_guys.mobile_clicker.presentation.PresentationService
import com.awsm_guys.mobile_clicker.presentation.clicker.MobileClicker
import com.awsm_guys.mobile_clicker.presentation.poko.Header
import com.awsm_guys.mobile_clicker.presentation.poko.Message
import com.awsm_guys.mobile_clicker.presentation.poko.Page
import com.awsm_guys.mobile_clicker.presentation.viewinteractor.Close
import com.awsm_guys.mobile_clicker.presentation.viewinteractor.SwitchPage
import com.awsm_guys.mobile_clicker.presentation.viewinteractor.ViewEvent
import com.awsm_guys.mobile_clicker.presentation.viewinteractor.ViewInteractor
import com.awsm_guys.mobile_clicker.utils.LoggingMixin
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class WebViewInteractor @Autowired constructor(
        private val presentationService: PresentationService,
        private val simpMessagingTemplate: SimpMessagingTemplate
) : ViewInteractor, ControllerListener, WebSocketListener, LoggingMixin {

    private val eventsSubject = PublishSubject.create<ViewEvent>()

    private lateinit var sessionId: String

    private val compositeDisposable = CompositeDisposable()
    private val objectMapper = jacksonObjectMapper()

    @PostConstruct
    fun init() {
        presentationService.attachViewInteractor(this)
    }

    @PreDestroy
    fun onDestroy(){
        compositeDisposable.clear()
    }

    override fun switchPage(page: Page) {
        log("view switching page to ${page.number}")
        simpMessagingTemplate.convertAndSend("/$sessionId", Message(
                Header.SWITCH_PAGE,
                page.number.toString(),
                mutableMapOf("imageBase64String" to page.imageBase64String)
        ))
    }

    override fun verifyMobileClicker(clicker: MobileClicker): Observable<Boolean> = Observable.just(true)

    override fun getEventsObservable(): Observable<ViewEvent> = eventsSubject.hide()

    override fun notifyClickerDisconnected() {
        log("notifyClickerDisconnected")
    }

    //web socket listeners

    override fun onConnected(event: SessionConnectEvent) {
    }

    override fun onDisconnected(event: SessionDisconnectEvent) {
        eventsSubject.onNext(Close)
    }

    override fun onMessageReceived(message: Message) {
        eventsSubject.onNext(SwitchPage(message.body.toInt()))
    }

    //controller listener

    override fun startPresentation(filePath: String) =
            presentationService.startPresentation(filePath)
                    .also {
                        sessionId = it.sessionId
                    }
}