package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Generic._
import Information.Fingerprinting.TerranStrategies.TerranTimings.{ElevenRax_BarracksCompleteBy, ElevenRax_MarineCompleteBy}
import Lifecycle.With
import ProxyBwapi.Races.Terran
import Utilities.Time.Seconds

class Fingerprint8Rax extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.fiveRax),
  new FingerprintOr(
    With.fingerprints.bbs,
    new FingerprintCompleteBy (Terran.Barracks, ElevenRax_BarracksCompleteBy  - Seconds(8),                               1),
    new FingerprintCompleteBy (Terran.Marine,   ElevenRax_MarineCompleteBy    - Seconds(3),                               1),
    new FingerprintCompleteBy (Terran.Marine,   ElevenRax_MarineCompleteBy    - Seconds(3)  + Seconds(15),                2),
    new FingerprintCompleteBy (Terran.Marine,   ElevenRax_MarineCompleteBy    - Seconds(3)  + Seconds(30),                3),
    new FingerprintCompleteBy (Terran.Marine,   ElevenRax_MarineCompleteBy    - Seconds(3)  + Seconds(45),                4),
    new FingerprintArrivesBy  (Terran.Marine,   ElevenRax_MarineCompleteBy    - Seconds(3)                + Seconds(30),  1),
    new FingerprintArrivesBy  (Terran.Marine,   ElevenRax_MarineCompleteBy    - Seconds(3)  + Seconds(15) + Seconds(30),  2),
    new FingerprintArrivesBy  (Terran.Marine,   ElevenRax_MarineCompleteBy    - Seconds(3)  + Seconds(30) + Seconds(30),  3),
    new FingerprintArrivesBy  (Terran.Marine,   ElevenRax_MarineCompleteBy    - Seconds(3)  + Seconds(45) + Seconds(30),  4)))