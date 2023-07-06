package Mathematics.Functions

/**
  * Perform integer division, multiplication, and modulus by bit-shifting.
  *
  * Bit-shifting is a bit faster than multiplication and MUCH faster than division.
  * The compiler can't automatically apply this optimization because it only works on whole numbers.
  *
  * Failure to inline these would be a performance disaster,
  * but these are exactly the kinds of functions the compiler is likely to inline without guidance anyway.
  */
trait Bitshift {
  @inline final def div2    (x: Int): Int = x >> 1
  @inline final def div4    (x: Int): Int = x >> 2
  @inline final def div8    (x: Int): Int = x >> 3
  @inline final def div16   (x: Int): Int = x >> 4
  @inline final def div32   (x: Int): Int = x >> 5
  @inline final def div64   (x: Int): Int = x >> 6
  @inline final def div128  (x: Int): Int = x >> 7
  @inline final def div256  (x: Int): Int = x >> 8

  @inline final def x2      (x: Int): Int = x << 1
  @inline final def x4      (x: Int): Int = x << 2
  @inline final def x8      (x: Int): Int = x << 3
  @inline final def x16     (x: Int): Int = x << 4
  @inline final def x32     (x: Int): Int = x << 5
  @inline final def x64     (x: Int): Int = x << 6
  @inline final def x128    (x: Int): Int = x << 7
  @inline final def x256    (x: Int): Int = x << 8

  @inline final def mod2    (x: Int): Int = x & 1
  @inline final def mod4    (x: Int): Int = x & 3
  @inline final def mod8    (x: Int): Int = x & 7
  @inline final def mod16   (x: Int): Int = x & 15
  @inline final def mod32   (x: Int): Int = x & 31
  @inline final def mod64   (x: Int): Int = x & 63
  @inline final def mod128  (x: Int): Int = x & 127
  @inline final def mod256  (x: Int): Int = x & 255
}
