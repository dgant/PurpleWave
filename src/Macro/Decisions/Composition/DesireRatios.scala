package Macro.Decisions.Composition

import ProxyBwapi.Races.{Protoss, Zerg}

object DesireRatios {
  
  lazy val ratios: Vector[Ratio] = Vector (
    RatioEnemy(Protoss.Zealot,        Zerg.Zergling,    0.4),
    RatioEnemy(Protoss.DarkTemplar,   Zerg.Zergling,    0.1),
    RatioEnemy(Protoss.HighTemplar,   Zerg.Zergling,    0.1),
    RatioEnemy(Protoss.Archon,        Zerg.Zergling,    0.2),
    RatioEnemy(Protoss.Reaver,        Zerg.Zergling,    0.1),
    RatioEnemy(Protoss.Scout,         Zerg.Zergling,    0.1),
    RatioEnemy(Protoss.Carrier,       Zerg.Zergling,    0.1),
    
    RatioEnemy(Protoss.Zealot,        Zerg.Hydralisk,   1.0),
    RatioEnemy(Protoss.Dragoon,       Zerg.Hydralisk,   0.1),
    RatioEnemy(Protoss.DarkTemplar,   Zerg.Zergling,    0.2),
    RatioEnemy(Protoss.HighTemplar,   Zerg.Hydralisk,   0.3),
    RatioEnemy(Protoss.Archon,        Zerg.Hydralisk,   0.2),
    RatioEnemy(Protoss.Reaver,        Zerg.Hydralisk,   0.2),
    
    RatioEnemy(Protoss.Dragoon,       Zerg.Mutalisk,    0.5),
    RatioEnemy(Protoss.Corsair,       Zerg.Mutalisk,    1.5),
    RatioEnemy(Protoss.Scout,         Zerg.Mutalisk,    0.2),
    RatioEnemy(Protoss.Carrier,       Zerg.Mutalisk,    0.2),
    
    RatioEnemy(Protoss.Zealot,        Zerg.Ultralisk,   3.0),
    RatioEnemy(Protoss.Dragoon,       Zerg.Ultralisk,   3.0),
    RatioEnemy(Protoss.DarkTemplar,   Zerg.Zergling,    0.5),
    RatioEnemy(Protoss.HighTemplar,   Zerg.Ultralisk,   0.8),
    RatioEnemy(Protoss.Archon,        Zerg.Ultralisk,   1.0),
    RatioEnemy(Protoss.Reaver,        Zerg.Ultralisk,   0.5),
    
    RatioEnemy(Protoss.Dragoon,       Zerg.Lurker,      2.0),
    RatioEnemy(Protoss.HighTemplar,   Zerg.Lurker,      0.7),
    RatioEnemy(Protoss.Observer,      Zerg.Lurker,      0.5),
    RatioEnemy(Protoss.Reaver,        Zerg.Lurker,      0.5),
    RatioEnemy(Protoss.Scout,         Zerg.Lurker,      0.1),
    RatioEnemy(Protoss.Carrier,       Zerg.Lurker,      0.2),
    
    RatioEnemy(Protoss.HighTemplar,   Zerg.Guardian,    1.0),
    RatioEnemy(Protoss.Corsair,       Zerg.Guardian,    1.0),
    RatioEnemy(Protoss.Scout,         Zerg.Guardian,    0.5)
  )
}
