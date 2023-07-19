package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Generic.{FingerprintArrivesBy, FingerprintOr}
import ProxyBwapi.Races.Terran
import Utilities.Time.GameTime

class Fingerprint3FacVultures extends FingerprintOr(
  // You can actually get 3 arriving by 5:13 by making 3 Vultures in a row out of an ordinary Factory timing
  //new FingerprintArrivesBy(Terran.Vulture, GameTime(5, 30), 3),
  new FingerprintArrivesBy(Terran.Vulture, GameTime(5, 50), 5),
  new FingerprintArrivesBy(Terran.Vulture, GameTime(6, 10), 8),
)