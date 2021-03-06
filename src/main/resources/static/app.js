var stompClient = null;
var presentationInfo = null;
var socketEndpoint = null;
var rootEndpoint = '/listener';
var currentSlideNumber = 0;

function connect() {
    var socket = new SockJS(rootEndpoint);
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        //incoming message listening
        stompClient.subscribe(socketEndpoint, function (stompMessage) {
            var message = JSON.parse(stompMessage.body);
            if (message.header.toString() === 'SWITCH_PAGE') {
                replaceSlideImage(message.features['imageBase64String']);
                currentSlideNumber = parseInt(message.body);
            }
        });
    });
}

function sendMessage(message){
    stompClient.send("/send", {}, message);
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
}

function startPresentation() {
    $.post("http://localhost:8080/start_presentation", {'filePath': 'C:\\Users\\hekpo\\Downloads\\Presentation_SchukinVV.pdf'})
        .done(function (e) {
            presentationInfo = e;
            console.log(e);
            replaceSlideImage(e.firstPage.imageBase64String);
            socketEndpoint = '/'+ presentationInfo.sessionId;
            connect();
        })
        .fail(function (error) {
            console.log("Error");
            console.log(error);
        });
}

function switchOnNext() {
    sendMessage(JSON.stringify({
        'header': 'SWITCH_PAGE',
        'body' : (currentSlideNumber + 1).toString(),
        'features': {}
    }))
}

function switchOnPrevious() {
    sendMessage(JSON.stringify({
        'header': 'SWITCH_PAGE',
        'body' : (currentSlideNumber - 1).toString(),
        'features': {}
    }))
}

function bye() {
    $.post("http://localhost:8080/bye", {})
        .done(function (e) {
            console.log(e);
            // id = e.translationId;
        })
        .fail(function (error) {
            console.log("Error");
            console.log(error);
        });
}

$(function () {
    $("form").on('submit', function (e) { e.preventDefault(); });
    $( "#start" ).click(function() { startPresentation(); });
    $( "#next_slide" ).click(function() { switchOnNext(); });
    $( "#prev_slide" ).click(function() { switchOnPrevious(); });
});

window.onload = function() {
    window.addEventListener("beforeunload", function (e) {
        bye();
    });
};

function replaceSlideImage(base64) {
    $('#slide_img').attr('src', 'data:image/png;base64,' + base64);
}