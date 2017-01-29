/*
 *  (c) K.Bryson, Dept. of Computer Science, UCL (2016)
 *  
 *  YOU MAY MODIFY THIS CLASS TO IMPLEMENT Stop & Wait ARQ PROTOCOL.
 *  (You will submit this class to Moodle.)
 *  
 */
package physical_network;

/**
 * Encapsulates the data for a network 'data frame'. At the moment this just
 * includes a payload byte array. This may need to be extended to include
 * necessary header information.
 *
 * @author kevin-b
 *
 */
public class DataFrame {

	public final byte[] payload;
	private int destination = 0;
	private byte comms = 0;
	private int sour = 0;
	private int ack = 0;
	private int seq = 0;
	private byte[] framed = null;
	//private int ackseq = 0;

	public DataFrame(String payload) {
		this.payload = payload.getBytes();
	}

	public DataFrame(String payload, int destination) {
		this.payload = payload.getBytes();
		this.destination = destination;
	}

	public DataFrame(int destination){
		this.payload = null;
		this.destination = destination;
	}

	public DataFrame(byte[] payload) {
		this.payload = payload;
	}

	public DataFrame(byte[] payload, int destination) {
		this.payload = payload;
		this.destination = destination;
	}
	public int getDestination() {
		return destination;
	}

	public byte[] getPayload() {
		return payload;
	}

	public String toString() {
		return new String(payload);
	}

	public int getLength(){
		if(framed != null){
			//System.out.println("length of frame"+framed.length);
			return framed.length;
		}else{
		return 0;}
	}

	/*
     * A factory method that can be used to create a data frame
     * from an array of bytes that have been received.
     */
	public static DataFrame createFromReceivedBytes(byte[] byteArray) {

		DataFrame created = new DataFrame(byteArray);

		return created;
	}

	/*
     * This method should return the byte sequence of the transmitted bytes.
     * At the moment it is just the payload data ... but extensions should
     * include needed header information for the data frame.
     * Note that this does not need sentinel or byte stuffing
     * to be implemented since this is carried out as the data
     * frame is transmitted and received.
     */
	public byte[] getTransmittedBytes(int so, int ack2, int seq2) {
		sour = so;
		ack = ack2;

		if(ack == 1){//message not acknowledgement and does seq and ackseq value change
			if(seq2 == 0){
				seq = 1;
			}else{
				seq = 0;
			}
		}else {//acknowledgement
			if(seq2 == 0){
				seq = 0;
			}else{
				seq = 1;
			}
		}

		String temp = "000000"+ack+seq;//converting to byte, just making sure
		int temp2 = Integer.parseInt(temp,2);
		comms = (byte)temp2;
		byte dest = (byte)this.destination;

		if(this.payload == null){//for acks
			framed = new byte[1];
			framed[0] = (byte)0;
			framed = concatfirst(comms, framed);//ack+seq+len
		}else {
			framed = concatfirst((byte) this.payload.length, this.payload);//len+ pay
			framed = concatfirst(comms, framed);//ack+seq+len+pay
		}

		framed = concatfirst((byte)sour, framed);//source+ackstuff +len+ payload
		framed = concatfirst(dest, framed);//dest+framed

		if(framed.length%2 != 0){//if theres not an even number of bytes for the checksum
			framed = concatend(framed,(byte)0x0000);
		}

		int check = checksum(framed);
		byte[] checkarray;
		checkarray = intToByte(check);
		framed = concatend(framed,checkarray[0]);
		framed = concatend(framed, checkarray[1]);
		return framed;
	}

	//int to byte[] for checksum
	private byte[] intToByte(int convert){
		return new byte[]{
				(byte)(convert>>>8),
				(byte)(convert)
		};

	}

	/*returns a byte[] that adds the byte to the end of the byte[]
	 */
	private static byte[] concatend(byte[] a1, byte a2){
		int length = a1.length + 1;
		byte[] result = new byte[length];
		int pos = 0;

		for (byte element : a1) {
			result[pos] = element;
			pos++;
		}
		result[pos] = a2;

		return result;

	}
	/*returns a byte[] that adds the byte at the front of the byte[]
	 */
	private static byte[] concatfirst(byte a1, byte[] a2){
		int length = a2.length + 1;
		byte[] result = new byte[length];
		int pos = 0;
		result[pos] = a1;
		pos++;
		for (byte element : a2) {
			result[pos] = element;
			pos++;
		}


		return result;

	}
	/*calculates the checksum for a byte[] and returns the checksum as an int
	 */
	private int checksum(byte[] load) {
		int length = load.length;
		int i = 0;

		int sum = 0;
		int data;

		// Pairs
		while (length > 1) {
			data = (((load[i] << 8) & 0xFF00) | ((load[i + 1]) & 0xFF));
			sum += data;
			if ((sum & 0xFFFF0000) > 0) {
				sum = sum & 0xFFFF;
				sum += 1;//wrap around
			}
			i += 2;
			length -= 2;
		}

		sum = ~sum;//returns the ones complement
		sum = sum & 0xFFFF;
		return sum;

	}
}
