package Planning.Plans.Recruitment

import ProxyBwapi.Races.{Protoss, Terran, Zerg}

object ResumeFeatures {
  
  val allSplash = Vector(
    Terran.ScienceVessel,
    Protoss.Archon,
    Protoss.HighTemplar,
    Protoss.Arbiter,
    Protoss.DarkArchon,
    Zerg.Defiler
  )
  
  val airSplash = Vector(
    Terran.Valkyrie,
    Protoss.Corsair,
    Zerg.Devourer
  )
  
  val groundSplash = Vector(
    Terran.Firebat,
    Terran.SiegeTankSieged,
    Terran.SiegeTankUnsieged,
    Protoss.Reaver,
    Zerg.Lurker
  )
  
  val siege = Vector(
    Terran.SiegeTankSieged,
    Terran.SiegeTankUnsieged,
    Terran.Battlecruiser,
    Protoss.Reaver,
    Protoss.Carrier,
    Zerg.Guardian,
    Zerg.Defiler
  )
}
