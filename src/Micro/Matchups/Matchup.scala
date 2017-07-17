package Micro.Matchups

import ProxyBwapi.UnitInfo.UnitInfo

case class Matchup(from: UnitInfo, to: UnitInfo) {
  
  val against = Matchup(to, from)
  def vpf: Double = from.dpfAgainst(to)
}
