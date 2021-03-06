package fi.iki.atr

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.util.*


@RestController
class BotController {
    private val restTemplate = RestTemplate()
    private var myBotId: UUID? = null
    private var botVersion = 1


    @PostMapping("/bot")
    fun registerBot() {
        myBotId = registerPlayer("Arin Kotlin Bot " + botVersion).id
        botVersion += 1
    }

    private fun registerPlayer(playerName: String): RegisterResponse {
        val myRestApiAddress = MY_REST_API_ADDRESS
        val registration = Registration(playerName, myRestApiAddress)
        val result = restTemplate.postForEntity<RegisterResponse>(SERVER_ADDRESS + "/register", registration, RegisterResponse::class.java)
        return result.body
    }


    @PostMapping(MY_REST_API_PATH)
    fun move(@RequestBody gameStateChanged: GameStateChanged): Move {
        val myPlayer = gameStateChanged.playerState
        val items = gameStateChanged.gameState.items
        val map = gameStateChanged.gameState.map

        System.out.println("My player is at " + myPlayer.position.x + ", " + myPlayer.position.y)
        System.out.println("The map has " + items.size + " items")
        System.out.println("The map consists of " + map.tiles.size + " x " + map.tiles[0].length + " tiles")

        return determineMove(myPlayer, map, items)
    }

    private fun determineMove(myPlayer: Player, map: Map, items: Set<Item>): Move {

        if (!myPlayer.usableItems.isEmpty()) {
            return Move.USE
        }

        var targetItem =  items.sortedBy { item -> item.position.distance(myPlayer.position) }.
                firstOrNull()

        var target : Position
        val goToExit = targetItem == null || myPlayer.money < 4000

        if (goToExit) {
            target = map.exit
        } else {
            if (targetItem != null) {
                target = targetItem.position
            } else {
                return Move.PICK
            }
        }

        if (myPlayer.position.x < target.x) {
            return Move.RIGHT
        } else if (myPlayer.position.x > target.x) {
            return Move.LEFT
        } else if (myPlayer.position.y < target.y) {
            return Move.DOWN
        } else if (myPlayer.position.y > target.y) {
            return Move.UP
        }
        return Move.PICK
    }

    data class Map(val width: Int,
              val height: Int,
              val tiles: List<String>,
              val exit: Position)

    data class Player(val position: Position,
                      val name: String,
                      val url: String,
                      val score: Int,
                      val money: Int,
                      val health: Int,
                      val usableItems: List<Item>)

    enum class Move {
        UP,
        DOWN,
        RIGHT,
        LEFT,
        PICK,
        USE // valid if the item is usable
    }

    data class Item(val price: Int,
                    val position: Position,
                    val type: Type,
                    val isUsable: Boolean) {


        enum class Type {
            JUST_SOME_JUNK,
            WEAPON
        }
    }

    data class Position(val x: Int,
                   val y: Int)      {
        fun distance(another : Position) = Math.sqrt(Math.pow((another.x - x).toDouble(), 2.0) + Math.pow((another.y - y).toDouble(), 2.0))
    }

    private class Registration(val playerName: String, val url: String)

    data class RegisterResponse(val id: UUID,
                                val player: Player,
                                val gameState: GameState)

    data class GameStateChanged(val gameState: GameState, val playerState: Player)

    data class GameState(val map: Map,
                         val players: Set<Player>,
                         val items: Set<Item>)

    companion object {

        // The map consists of tiles with one of the following type:
        private val WALL_TILE = 'x'
        private val FLOOR_TILE = '_'
        private val EXIT_TILE = 'o'

        // FIXME use correct server IP address
        const val SERVER_ADDRESS = "http://192.168.1.2:8080"

        const val MY_REST_API_PATH = "/move"
        const val MY_REST_API_ADDRESS = "http://192.168.1.3:9080" + MY_REST_API_PATH
    }
}
