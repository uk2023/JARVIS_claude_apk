package com.example.data

import android.content.Context
import com.example.data.db.ChatSessionEntity
import com.example.data.db.ClaudeDatabase
import com.example.data.db.SessionDao
import com.example.data.db.TurnRecordEntity
import com.example.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class TurnRepository(context: Context) {
    private val sessionDao: SessionDao = ClaudeDatabase.getDatabase(context).sessionDao()

    val allSessions: Flow<List<ChatSessionEntity>> = sessionDao.getAllSessions()

    fun getTurns(sessionId: String): Flow<List<TurnState>> {
        return sessionDao.getTurnsForSession(sessionId).map { list ->
            list.map { entity -> entityToTurnState(entity) }
        }
    }

    suspend fun saveSession(id: String, title: String, modelId: String) {
        sessionDao.insertSession(
            ChatSessionEntity(
                id = id,
                title = title,
                modelId = modelId
            )
        )
    }

    suspend fun saveTurn(sessionId: String, turn: TurnState) {
        sessionDao.insertTurn(turnStateToEntity(sessionId, turn))
    }

    suspend fun deleteSession(sessionId: String) {
        sessionDao.deleteTurnsForSession(sessionId)
        sessionDao.deleteSession(sessionId)
    }

    private fun turnStateToEntity(sessionId: String, turn: TurnState): TurnRecordEntity {
        val attachmentsArr = JSONArray()
        turn.promptAttachments.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("name", it.name)
            obj.put("size", it.size)
            obj.put("extension", it.extension)
            attachmentsArr.put(obj)
        }

        val stepGroupsArr = JSONArray()
        turn.stepGroups.forEach { group ->
            val gObj = JSONObject()
            gObj.put("id", group.id)
            gObj.put("isClosed", group.isClosed)
            if (group.bridgeText != null) {
                gObj.put("bridgeText", group.bridgeText)
            }
            val stepsArr = JSONArray()
            group.steps.forEach { s ->
                val sObj = JSONObject()
                sObj.put("id", s.id)
                sObj.put("label", s.label)
                sObj.put("toolType", s.toolType.name)
                sObj.put("status", s.status.name)
                sObj.put("details", s.details)
                sObj.put("timestamp", s.timestamp)
                stepsArr.put(sObj)
            }
            gObj.put("steps", stepsArr)
            stepGroupsArr.put(gObj)
        }

        val delArr = JSONArray()
        turn.deliverables.forEach { d ->
            val dObj = JSONObject()
            dObj.put("id", d.id)
            dObj.put("filename", d.filename)
            dObj.put("size", d.size)
            dObj.put("extension", d.extension)
            dObj.put("description", d.description)
            dObj.put("previewContent", d.previewContent)
            delArr.put(dObj)
        }

        val summaryObj = JSONObject()
        turn.turnSummary?.let { s ->
            val doneArr = JSONArray()
            s.whatWasDone.forEach { doneArr.put(it) }
            val delivsArr = JSONArray()
            s.deliverablesProduced.forEach { delivsArr.put(it) }
            val nextArr = JSONArray()
            s.nextSteps.forEach { nextArr.put(it) }

            summaryObj.put("whatWasDone", doneArr)
            summaryObj.put("deliverablesProduced", delivsArr)
            summaryObj.put("nextSteps", nextArr)
        }

        return TurnRecordEntity(
            id = turn.turnId,
            sessionId = sessionId,
            userPrompt = turn.userPrompt,
            attachmentsJson = attachmentsArr.toString(),
            stepGroupsJson = stepGroupsArr.toString(),
            finalTitle = turn.finalResponseTitle,
            finalResponseText = turn.finalResponseText,
            deliverablesJson = delArr.toString(),
            turnSummaryJson = summaryObj.toString(),
            status = turn.status.name,
            timestamp = turn.timestamp
        )
    }

    private fun entityToTurnState(entity: TurnRecordEntity): TurnState {
        val attachments = mutableListOf<Attachment>()
        try {
            val arr = JSONArray(entity.attachmentsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                attachments.add(
                    Attachment(
                        id = obj.optString("id", ""),
                        name = obj.optString("name", ""),
                        size = obj.optString("size", ""),
                        extension = obj.optString("extension", "")
                    )
                )
            }
        } catch (_: Exception) {}

        val stepGroups = mutableListOf<StepGroup>()
        try {
            val arr = JSONArray(entity.stepGroupsJson)
            for (i in 0 until arr.length()) {
                val gObj = arr.getJSONObject(i)
                val steps = mutableListOf<Step>()
                val sArr = gObj.optJSONArray("steps")
                if (sArr != null) {
                    for (j in 0 until sArr.length()) {
                        val sObj = sArr.getJSONObject(j)
                        val toolName = sObj.optString("toolType", "COMMAND")
                        val toolType = try { ToolType.valueOf(toolName) } catch (_: Exception) { ToolType.COMMAND }
                        val statusName = sObj.optString("status", "COMPLETED")
                        val status = try { StepStatus.valueOf(statusName) } catch (_: Exception) { StepStatus.COMPLETED }
                        steps.add(
                            Step(
                                id = sObj.optString("id", ""),
                                label = sObj.optString("label", ""),
                                toolType = toolType,
                                status = status,
                                timestamp = sObj.optLong("timestamp", System.currentTimeMillis()),
                                details = sObj.optString("details", "")
                            )
                        )
                    }
                }
                stepGroups.add(
                    StepGroup(
                        id = gObj.optString("id", "group-$i"),
                        steps = steps,
                        bridgeText = if (gObj.has("bridgeText")) gObj.getString("bridgeText") else null,
                        isClosed = gObj.optBoolean("isClosed", true)
                    )
                )
            }
        } catch (_: Exception) {}

        val deliverables = mutableListOf<Deliverable>()
        try {
            val arr = JSONArray(entity.deliverablesJson)
            for (i in 0 until arr.length()) {
                val dObj = arr.getJSONObject(i)
                deliverables.add(
                    Deliverable(
                        id = dObj.optString("id", ""),
                        filename = dObj.optString("filename", ""),
                        size = dObj.optString("size", ""),
                        extension = dObj.optString("extension", "tar.gz"),
                        description = dObj.optString("description", ""),
                        previewContent = dObj.optString("previewContent", "")
                    )
                )
            }
        } catch (_: Exception) {}

        var turnSummary: TurnSummary? = null
        try {
            val sObj = JSONObject(entity.turnSummaryJson)
            if (sObj.has("whatWasDone")) {
                val doneList = mutableListOf<String>()
                val dArr = sObj.optJSONArray("whatWasDone")
                if (dArr != null) {
                    for (i in 0 until dArr.length()) doneList.add(dArr.getString(i))
                }
                val delList = mutableListOf<String>()
                val delArr = sObj.optJSONArray("deliverablesProduced")
                if (delArr != null) {
                    for (i in 0 until delArr.length()) delList.add(delArr.getString(i))
                }
                val nextList = mutableListOf<String>()
                val nArr = sObj.optJSONArray("nextSteps")
                if (nArr != null) {
                    for (i in 0 until nArr.length()) nextList.add(nArr.getString(i))
                }
                turnSummary = TurnSummary(doneList, delList, nextList)
            }
        } catch (_: Exception) {}

        val turnStatus = try { TurnStatus.valueOf(entity.status) } catch (_: Exception) { TurnStatus.COMPLETED }

        return TurnState(
            turnId = entity.id,
            userPrompt = entity.userPrompt,
            promptAttachments = attachments,
            activeActivityLabel = null,
            activeActivityIcon = "clock",
            stepGroups = stepGroups,
            finalResponseTitle = entity.finalTitle,
            finalResponseText = entity.finalResponseText,
            deliverables = deliverables,
            turnSummary = turnSummary,
            status = turnStatus,
            timestamp = entity.timestamp
        )
    }
}
