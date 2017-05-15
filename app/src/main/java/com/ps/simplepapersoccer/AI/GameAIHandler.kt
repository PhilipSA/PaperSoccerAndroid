package com.ps.simplepapersoccer.AI

import co.metalab.asyncawait.async
import com.ps.simplepapersoccer.GameObjects.Game.GameHandler
import com.ps.simplepapersoccer.GameObjects.Player.AIPlayer

class GameAIHandler(private val gameHandler: GameHandler, private val calculateAsync: Boolean) {

    fun MakeAIMove(aiPlayer : AIPlayer) {
        if(calculateAsync) {
            MakeAIMoveAsync(aiPlayer)
            return
        }
        else {
            gameHandler.AIMakeMove(aiPlayer.gameAI?.MakeMove(gameHandler)!!)
        }
    }

    fun MakeAIMoveAsync(aiPlayer : AIPlayer) = async {
        var aiMove = await { aiPlayer.gameAI?.MakeMove(gameHandler) }
        gameHandler.AIMakeMove(aiMove!!)
    }
}
