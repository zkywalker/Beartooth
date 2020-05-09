package org.zky.beartooth.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ByteUtils {
    public static byte[] getBytes(short data) {

        byte[] bytes = new byte[2];
//      bytes[0] = (byte) (data & 0xff);
//      bytes[1] = (byte) ((data & 0xff00) >> 8);
        //高低位互换 //YAO 20190313
        bytes[1] = (byte) (data & 0xff);
        bytes[0] = (byte) ((data & 0xff00) >> 8);
        return bytes;
    }

    public static byte[] getBytes(char data) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (data);
        bytes[1] = (byte) (data >> 8);
        return bytes;
    }

    public  static double[] byteArrayToDoubleArray(byte[] data){
        double[] doubles = new double[data.length / 2];
        for (int i = 0; i < data.length / 2; i++)
        {
            doubles[i] = data[i*2] & 0xFF | (data[(i*2 + 1)] & 0xFF) << 8;
        }
        return  doubles;
    }

    public static byte[] intToByteArray(int data){
        return new byte[] { (byte)data, (byte)(data >> 8), (byte)(data >> 16), (byte)(data >> 24) };
    }

    public static byte[] getBytes(int data) {
        byte[] bytes = new byte[4];
//        bytes[0] = (byte) (data & 0xff);
//        bytes[1] = (byte) ((data & 0xff00) >> 8);
//        bytes[2] = (byte) ((data & 0xff0000) >> 16);
//        bytes[3] = (byte) ((data & 0xff000000) >> 24);
        //YAO 高低位互换
        bytes[3] = (byte) (data & 0xff);
        bytes[2] = (byte) ((data & 0xff00) >> 8);
        bytes[1] = (byte) ((data & 0xff0000) >> 16);
        bytes[0] = (byte) ((data & 0xff000000) >> 24);

        return bytes;
    }

    public static byte[] getBytes(long data) {
        byte[] bytes = new byte[8];
//        bytes[0] = (byte) (data & 0xff);
//        bytes[1] = (byte) ((data >> 8) & 0xff);
//        bytes[2] = (byte) ((data >> 16) & 0xff);
//        bytes[3] = (byte) ((data >> 24) & 0xff);
//        bytes[4] = (byte) ((data >> 32) & 0xff);
//        bytes[5] = (byte) ((data >> 40) & 0xff);
//        bytes[6] = (byte) ((data >> 48) & 0xff);
//        bytes[7] = (byte) ((data >> 56) & 0xff);
        //YAO 高低位互换
        bytes[7] = (byte) (data & 0xff);
        bytes[6] = (byte) ((data >> 8) & 0xff);
        bytes[5] = (byte) ((data >> 16) & 0xff);
        bytes[4] = (byte) ((data >> 24) & 0xff);
        bytes[3] = (byte) ((data >> 32) & 0xff);
        bytes[2] = (byte) ((data >> 40) & 0xff);
        bytes[1] = (byte) ((data >> 48) & 0xff);
        bytes[0] = (byte) ((data >> 56) & 0xff);
        return bytes;
    }

    public static byte[] getBytes(float data) {
        int intBits = Float.floatToIntBits(data);
        return getBytes(intBits);
    }

    public static byte[] getBytes(double data) {
        long intBits = Double.doubleToLongBits(data);
        return getBytes(intBits);
    }

    public static byte[] getBytes(double[] data) {
        int times = Double.SIZE / Byte.SIZE;
        byte[] bytes = new byte[data.length * times];
        for (int i = 0; i < data.length; i++) {
            ByteBuffer.wrap(bytes, i * times, times).putDouble(data[i]);
        }
        return bytes;
    }

    public static byte[] getBytes(String data, String charsetName) {
        Charset charset = Charset.forName(charsetName);
        return data.getBytes(charset);
    }

    public static byte[] getBytes(String data) {
        return getBytes(data, "GBK");
    }

    public static byte[] getBytes(List<Byte> list) {
        byte[] ret = new byte[list.size()];
        int i = 0;
        for (Byte b : list)
            ret[i++] = b.byteValue();
        return ret;
    }

    public static short getShort(byte[] bytes) {
        return (short) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
    }

    public static char getChar(byte[] bytes) {
        return (char) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
    }

    public static int getInt(byte[] bytes) {
        return (0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)) | (0xff0000 & (bytes[2] << 16)) | (0xff000000 & (bytes[3] << 24));
    }

    public static long getLong(byte[] bytes) {
        return (0xffL & (long) bytes[0]) | (0xff00L & ((long) bytes[1] << 8)) | (0xff0000L & ((long) bytes[2] << 16)) | (0xff000000L & ((long) bytes[3] << 24))
                | (0xff00000000L & ((long) bytes[4] << 32)) | (0xff0000000000L & ((long) bytes[5] << 40)) | (0xff000000000000L & ((long) bytes[6] << 48)) | (0xff00000000000000L & ((long) bytes[7] << 56));
    }

    public static float getFloat(byte[] bytes) {
        return Float.intBitsToFloat(getInt(bytes));
    }

    public static double getDouble(byte[] bytes) {
        long l = getLong(bytes);
        return Double.longBitsToDouble(l);
    }

    public static String getString(byte[] bytes, String charsetName) {
        return new String(bytes, Charset.forName(charsetName));
    }

    public static String getString(byte[] bytes) {
        List<Byte> chars = new ArrayList<>();
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == 0) {
                break;
            } else {
                chars.add(bytes[i]);
            }
        }
        return getString(getBytes(chars), "UTF-8");
    }

    public static String[] getHexStrings(byte[] src) {
        if (src == null || src.length <= 0) {
            return null;
        }
        String[] str = new String[src.length];

        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                str[i] = "0";
            }
            str[i] = hv;
        }
        return str;
    }

    public static final short byteArrayToShort(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getShort();
    }

    public static final int byteArrayToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static final float byteArrayToFloat(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getFloat();
    }

    public static double byteArrayToDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }

    public static final long byteArrayToLong(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getLong();
    }

    public static byte[] toByteArray(double[] doubleArray) {
        int times = Double.SIZE / Byte.SIZE;
        byte[] bytes = new byte[doubleArray.length * times];
        for (int i = 0; i < doubleArray.length; i++) {
            ByteBuffer.wrap(bytes, i * times, times).putDouble(doubleArray[i]);
        }
        return bytes;
    }

    public static double[] toDoubleArray(byte[] byteArray) {
        int times = Double.SIZE / Byte.SIZE;
        double[] doubles = new double[byteArray.length / times];
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] = ByteBuffer.wrap(byteArray, i * times, times).getDouble();
        }
        return doubles;
    }

    public static byte[] toByteArray(int[] intArray) {
        int times = Integer.SIZE / Byte.SIZE;
        byte[] bytes = new byte[intArray.length * times];
        for (int i = 0; i < intArray.length; i++) {
            ByteBuffer.wrap(bytes, i * times, times).putInt(intArray[i]);
        }
        return bytes;
    }

    public static int[] toIntArray(byte[] byteArray) {
        int times = Integer.SIZE / Byte.SIZE;
        int[] ints = new int[byteArray.length / times];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = ByteBuffer.wrap(byteArray, i * times, times).getInt();
        }
        return ints;
    }

    public static byte[] doubleToByte(double data) {
        long value = Double.doubleToRawLongBits(data);
        byte[] byteRet = new byte[8];
        for (int i = 0; i < 8; i++) {
            byteRet[i] = (byte) ((value >> 8 * i) & 0xff);
        }
        return byteRet;
    }

    public static byte[] shortToByte(short data) {
        byte[] bytes = new byte[2];
        bytes[1] = (byte) (data & 0xff);
        bytes[0] = (byte) ((data & 0xff00) >> 8);
        return bytes;
    }

    public static String byte2hex(byte[] buffer, boolean appendBlank) {
        StringBuilder h = new StringBuilder();

        for (byte aBuffer : buffer) {
            String temp = Integer.toHexString(aBuffer & 0xFF);
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            h.append(appendBlank ? " " : "").append(temp);
        }
        return h.toString();
    }


    /**
     * convert byte to HexString
     *
     * @param b
     * @return
     */
    public static String byte2hex(byte b)
    {
        StringBuilder sb = new StringBuilder(1);
        String sTemp = Integer.toHexString(0xFF & b);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());

        return sb.toString();
    }


    public static void main(String[] args) {
        double[] dbs = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        byte[] byts = toByteArray(dbs);
        System.out.println(Arrays.toString(byts));
        double[] val = toDoubleArray(byts);
        System.out.println(Arrays.toString(val));
        String[] catenames = {"A0001", "A0003", "A0006", "A0007", "A0008", "A0009", "A00010"};
        for (int i = 0; i < catenames.length; i++) {
            System.out.print(catenames[i]);
        }
        System.out.println("");
        byte[] test = new byte[]{(byte)0xaa,(byte)0x55,(byte)0x07,(byte)0x27,(byte)0x01,(byte)0xdd,(byte)0x07,(byte)0xdb,(byte)0xbc};
        System.out.println(ByteUtils.byte2hex(test,true));
        byte[] test2 = new byte[test.length-2];
        System.arraycopy(test, 2, test2, 0, test2.length);

        System.out.println(ByteUtils.byte2hex(test2,true));

        System.out.println( ByteUtils.byte2hex(sumCheck(test2)));

    }

    public static char[] getChars(byte[] bytes) {
        Charset cs = Charset.forName("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);
        return cb.array();
    }

    /**
     * 截取byte数组   不改变原数组
     * @param b 原数组
     * @param off 偏差值（索引）
     * @param length 长度
     * @return 截取后的数组
     */
    public static byte[] subByte(byte[] b,int off,int length){
        byte[] b1 = new byte[length];
        System.arraycopy(b, off, b1, 0, length);
        return b1;
    }


    /**
     * 求校验和的算法
     * @param b 需要求校验和的字节数组
     * @return 校验和
     */
    public  static byte sumCheck(byte[] b) {
        int sum = 0;
        for (int i = 0; i < b.length; i++) {
            sum = sum + b[i];
        }
        if (sum > 0xff) { //超过了255，使用补码（补码 = 原码取反 + 1）
            sum = ~sum;
            sum = sum + 1;
        }
        return (byte) (sum & 0xff);

    }
}