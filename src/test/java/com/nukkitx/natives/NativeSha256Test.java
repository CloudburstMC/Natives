package com.nukkitx.natives;

public class NativeSha256Test {

//    private static final byte[] INPUT_1 = "Hello, world".getBytes();
//    private static final byte[] INPUT_2 = "NukkitX".getBytes();
//    private static final byte[] EXPECTED_HASH_1 = new byte[]{
//            74, -25, -61, -74, -84, 11, -17, -10, 113, -17, -88, -49, 87, 56, 97, 81, -64, 110, 88, -54, 83, -89, -115, -125, -13, 97, 7, 49, 108, -20, 18, 95
//    };
//    private static final byte[] EXPECTED_HASH_2 = new byte[]{
//            100, -42, 60, 123, 99, 64, 41, -42, -66, -73, -111, 55, 74, -95, -127, -32, 113, 91, -10, 78, 14, 42, -53, 46, 60, -51, 72, 61, 85, 72, -36, -119
//    };
//
//    private final NativeCode<NukkitHash> factory = new NativeCode<>("native-sha256", JavaSha256.class, NativeSha256.class);
//
//    @Test
//    public void doTest() {
//        if (NativeCode.isSupported()) {
//            Assert.assertTrue( "Native code failed to load!", factory.load() );
//            test( factory.newInstance() );
//        }
//        test(new JavaSha256());
//    }
//
//    private void test(NukkitHash hash)
//    {
//        System.out.println( "Testing: " + hash );
//
//        ByteBuf buf1 = Unpooled.directBuffer();
//        buf1.writeBytes(INPUT_1);
//        hash.update( buf1 );
//        byte[] out = hash.digest();
//
//        Assert.assertArrayEquals( "First sha256 does not match", EXPECTED_HASH_1, out );
//
//        // Test multiple hashes with same instance
//        ByteBuf buf2 = Unpooled.directBuffer();
//        buf2.writeBytes(INPUT_2);
//        hash.update( buf2 );
//        byte[] out2 = hash.digest();
//
//        Assert.assertArrayEquals( "Second sha256 does not match", EXPECTED_HASH_2, out2 );
//
//        buf1.release();
//        buf2.release();
//    }
}
