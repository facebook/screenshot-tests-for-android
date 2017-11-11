package com.facebook.screenshots

import java.io.Serializable

sealed class MessageType : Serializable {
    class Warning : MessageType()
    class Error : MessageType()
    class Success : MessageType()
}