package com.ahao.wifichat.protocol.v1

import com.google.gson.annotations.SerializedName

data class MsgAttachmentContent(
    var msg_id: Int = 0,
    @field:SerializedName("content")
    var attachment: MsgAttachment? = null,
)
