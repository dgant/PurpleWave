package ProxyBwapi.Players

import Lifecycle.With
import bwapi.Player

object Players {
  def all: Iterable[PlayerInfo] = With.proxy.players
  def get(player: Player): PlayerInfo = With.proxy.playersById(player.getID)
}
