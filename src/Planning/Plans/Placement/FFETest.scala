package Planning.Plans.Placement

import Planning.Plan
import ProxyBwapi.Races.Protoss

class FFETest extends Plan {

  override def onUpdate(): Unit = {
    val toPlace = Array(
      Protoss.Pylon,
      Protoss.Gateway,
      Protoss.Forge,
      Protoss.PhotonCannon,
      Protoss.PhotonCannon,
      Protoss.PhotonCannon,
      Protoss.PhotonCannon
    )
  }
}
