package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Zerg
import Utilities.GameTime

class Fingerprint9Pool extends FingerprintOr(
  With.fingerprints.ninePoolGas,
  new FingerprintAnd(
    new FingerprintNot(With.fingerprints.fourPool),
    new FingerprintOr(
      new FingerprintArrivesBy(Zerg.Zergling,       GameTime(3, 15)),
      new FingerprintCompleteBy(Zerg.SpawningPool,  GameTime(2, 2)))))
      // 9 Pool and Overpool are hard to differentiate because there's only a 10 second difference
      // to acquire the extra 100 minerals that Overpool requires for its Spawning Pool.
      // The boundary for 9 Pool completion looks to be around 1:58;
      // the extra four seconds should avoid false negatives
