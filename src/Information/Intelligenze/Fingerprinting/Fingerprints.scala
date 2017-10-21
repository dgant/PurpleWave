package Information.Intelligenze.Fingerprinting

import Information.Intelligenze.Fingerprinting.ProtossStrategies.{Fingerprint1GateCore, Fingerprint2Gate, FingerprintProxyGateway}
import Information.Intelligenze.Fingerprinting.ZergStrategies._

class Fingerprints {
  lazy val fingerprintProxyGateway = new FingerprintProxyGateway
  lazy val fingerprint2Gate        = new Fingerprint2Gate
  lazy val fingerprint1GateCore    = new Fingerprint1GateCore
  lazy val fingerprint4Pool        = new Fingerprint4Pool
  lazy val fingerprint9Pool        = new Fingerprint9Pool
  lazy val fingerprintOverpool     = new FingerprintOverpool
  lazy val fingerprint10Hatch9Pool = new Fingerprint10Hatch9Pool
  lazy val fingerprint12Pool       = new Fingerprint12Pool
  lazy val fingerprint12Hatch      = new Fingerprint12Hatch
}
