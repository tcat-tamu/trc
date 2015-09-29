package edu.tamu.tcat.trc.persist.postgres.id;

/**
 * Java implementation for generating Tiny URL- and bit.ly-like URLs. This is
 * ported from Michael Fogleman's Python based implementation.
 *
 * <p>A bit-shuffling approach is used to avoid generating consecutive, predictable
 * URLs.  However, the algorithm is deterministic and will guarantee that no
 * collisions will occur.</p>
 *
 * <p>The URL alphabet is fully customizable and may contain any number of
 * characters.  By default, digits and lower-case letters are used, with
 * some removed to avoid confusion between characters like o, O and 0.  The
 * default alphabet is shuffled and has a prime number of characters to further
 * improve the results of the algorithm.</p>
 *
 * <p>The block size specifies how many bits will be shuffled.  The lower BLOCK_SIZE
 * bits are reversed.  Any bits higher than BLOCK_SIZE will remain as is.
 * BLOCK_SIZE of 0 will leave all bits unaffected and the algorithm will simply
 * be converting your integer to a different base.</p>
 *
 * <p>The intended use is that incrementing, consecutive integers will be used as
 * keys to generate the short URLs.  For example, when creating a new URL, the
 * unique integer ID assigned by a database could be used to generate the URL
 * by using this module.  Or a simple counter may be used.  As long as the same
 * integer is not used twice, the same short URL will not be generated twice.</p>
 *
 * <p>The module supports both encoding and decoding of URLs. The min_length
 * parameter allows you to pad the URL if you want it to be a specific length.</p>
 *
 * <h1>Sample Usage:</h1>
>>> import short_url
>>> url = short_url.encode_url(12)
>>> print url
LhKA
>>> key = short_url.decode_url(url)
>>> print key
12
 *
 * <p>Use the functions in the top-level of the module to use the default encoder.
 * Otherwise, you may create your own UrlEncoder object and use its encode_url
 * and decode_url methods.
 *
 * @author Michael Fogleman (original)
 * @author Neal Audenaert (port to Java)
 * {@link http://code.activestate.com/recipes/576918/}
 *
 *
 * License: MIT
 */
public class IdObfuscator
{
//    private static final String ALPHABET = "mn6j2c4rv8bpygw95z7hsdaetxuk3fq";
//    private static final int BLOCK_SIZE = 24;
//    private static final int MIN_LENGTH = 5;

    private final String alphabet;      /** The alphabet that defines the base system to encode number into. */
    private final int blockSize;        /** The number of bits to shuffle. */
    private final int minLength;
    private final long mask;
    private final int[] mapping;

    public IdObfuscator(String alphabet, int blockSize, int minLength) {
        this.alphabet = alphabet;
        this.blockSize = blockSize;
        this.minLength = minLength;

        this.mask = (1 << this.blockSize) - 1;
        this.mapping = new int[this.blockSize];
        for (int i = 0; i < this.blockSize; i++) {
            this.mapping[i] = this.blockSize - (i + 1);
        }
    }

    public String encode(long n) {
        return this.enbase(this.shuffle(n));
    }

    public long decode(String s) {
        return this.unshuffle(this.debase(s));
    }

    /**
     * This method performs the bit shuffling needed to avoid generating consecutive,
     * predictable URLs.
     *
     * @param n The number to be encoded
     * @return The supplied number with the lower order bits shuffled.
     */
    private long shuffle(long n) {
        long prefix = (n & ~this.mask); // The higher order bits

        // The lower order bits
        long postfix = 0;
        for (int i = 0; i < this.mapping.length; i++) {
            int b = this.mapping[i];
            if ((n & (1 << i)) > 0) {
                postfix |= (1 << b);
            }
        }

        return prefix | postfix;
    }

    private String pad(String result) {
        StringBuffer padding = new StringBuffer();
        for (int i = 0; i < minLength - result.length(); i++) {
            padding.append(alphabet.charAt(0));
        }

        return padding.toString() + result;
    }

    /**
     * Transforms a source number into a string given a based on the base system
     * defined by the alphabet.
     *
     * @param x The number to enbase
     * @return The enbased number.
     */
    private String enbase(long x) {
        StringBuffer sb = new StringBuffer();
        int n = this.alphabet.length();

        while (x >= n) {
            sb.insert(0, this.alphabet.charAt((int)x % n));
            x = x / n;
        }

        sb.insert(0, this.alphabet.charAt((int)x));
        return this.pad(sb.toString());
    }

    private long unshuffle(long n) {
        long prefix = (n & ~this.mask); // The higher order bits

        // The lower order bits
        int postfix = 0;
        for(int i = 0; i < this.mapping.length; i++) {
            int b = this.mapping[i];
            if ((n & (1 << b)) > 0) {
                postfix |= (1 << i);
            }
        }

        return prefix | postfix;
    }

    private int debase(String x) {
        int n = this.alphabet.length();
        int result = 0;

        for (int i = 0; i < x.length(); i++) {
            char c = x.charAt(x.length() - (i + 1));
            result += this.alphabet.indexOf(c) * Math.pow(n,  i);
        }

        return result;
    }

//    private static void test() {
//        IdObfuscator s = new IdObfuscator(ALPHABET, BLOCK_SIZE, MIN_LENGTH);
//        for (long a = 0; a < 20000; a += 37) {
//            long b = s.shuffle(a);
//            String c = s.enbase(b);
//            long d = s.debase(c);
//            long e = s.unshuffle(d);
//
//            assert a == b;
//            assert b == d;
//
//            System.out.printf("%6d %12d %7s %12d %6d\n", a, b, c, d, e);
//        }
//    }
//
//    public static void main(String[] args) {
//        int CT = 200;
//        String[] ids = new String[CT];
//        IdObfuscator s = new IdObfuscator(ALPHABET, BLOCK_SIZE, MIN_LENGTH);
//
//        long start = System.currentTimeMillis();
//        for (long a = 0; a < CT; a++) {
//            ids[(int)a] = s.encode(a);
//            System.out.println(a + ": " + ids[(int)a]);
//        }
//        float ms = (float)(System.currentTimeMillis() - start) / CT;
//        System.out.println("Encoding IDs: " + ms + " ms");
//
//        long id;
//        start = System.currentTimeMillis();
//        for (int a = 0; a < CT; a++) {
//            id = s.decode(ids[a]);
//        }
//        ms = (float)(System.currentTimeMillis() - start) / CT;
//        System.out.println("Decoding IDs: " + ms + " ms");
//    }
}
