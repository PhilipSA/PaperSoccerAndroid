package com.ps.simplepapersoccer.AI

import co.metalab.asyncawait.async
import com.ps.simplepapersoccer.GameObjects.Game.GameHandler
import com.ps.simplepapersoccer.GameObjects.Move.PartialMove
import com.ps.simplepapersoccer.GameObjects.Player.AIPlayer

class GameAIHandler(private val gameHandler: GameHandler, private val calculateAsync: Boolean) {

    fun MakeAIMove(aiPlayer : AIPlayer) {
        var aiMove = if(calculateAsync) MakeAIMoveAsync(aiPlayer) else aiPlayer.gameAI?.MakeMove(gameHandler)
        gameHandler.AIMakeMove((aiMove as PartialMove?)!!)
    }

    fun MakeAIMoveAsync(aiPlayer : AIPlayer) = async {
        await { aiPlayer.gameAI?.MakeMove(gameHandler) }
    }
}
