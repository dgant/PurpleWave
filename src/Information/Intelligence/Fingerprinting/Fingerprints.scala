package Information.Intelligence.Fingerprinting

import Information.Intelligence.Fingerprinting.ProtossStrategies.{Fingerprint1GateCore, Fingerprint2Gate, FingerprintProxyGateway}
import Information.Intelligence.Fingerprinting.ZergStrategies._

class Fingerprints {
  val fingerprintProxyGateway = new FingerprintProxyGateway
  val fingerprint2Gate        = new Fingerprint2Gate
  val fingerprint1GateCore    = new Fingerprint1GateCore
  val fingerprint4Pool        = new Fingerprint4Pool
  val fingerprint9Pool        = new Fingerprint9Pool
  val fingerprintOverpool     = new FingerprintOverpool
  val fingerprint10Hatch9Pool = new Fingerprint10Hatch9Pool
  val fingerprint12Pool       = new Fingerprint12Pool
  val fingerprint12Hatch      = new Fingerprint12Hatch
}
