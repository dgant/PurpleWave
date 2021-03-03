package Micro.Squads

import Micro.Agency.Intention

class SquadChill extends Squad {
  override def run() {
    units.foreach(_.agent.intend(this, new Intention))
  }
}
