package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Generic.{FingerprintArrivesBy, FingerprintOr, GameTime}
import ProxyBwapi.Races.Terran

class Fingerprint3FacVultures extends FingerprintOr(
  new FingerprintArrivesBy(Terran.Vulture, GameTime(5, 30), 3),
  new FingerprintArrivesBy(Terran.Vulture, GameTime(5, 50), 5),
  new FingerprintArrivesBy(Terran.Vulture, GameTime(6, 10), 8),
)