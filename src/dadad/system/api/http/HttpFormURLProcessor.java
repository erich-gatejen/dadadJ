package dadad.system.api.http;

import java.io.Reader;
import java.util.Map;

import dadad.data.CharacterTool;
import dadad.platform.AnnotatedException;

/**
 * Processes POST form data.
 * 
 * This was adapted from THINGS, as I remember debugging the various browser quirks was very painful.  I will
 * address the license by releasing the original class into the public domain.
 */
public class HttpFormURLProcessor extends CharacterTool {

	// =========================================================================================================
	// PUBLIC METHODS
	
	// =========================================================================================================
	// DATA
	private String			REG_name;
	private String			REG_value;
	private Map<String, String>  	REG_nv;
	private	int				REG_sixteens;
	private boolean			FLAG_Done;
	private StringBuffer	ACCUMULATOR;
	private Reader 			reader;
	
	// =========================================================================================================
	// PRIVATE ENGINE	
	
	/**
	 * Parse engine grammar.<br><pre>
	 * 
POST /index.html HTTP/1.0
Accept: image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, SPLAT/SPLAT
Accept-Language: en-us
Content-Type: application/x-www-form-urlencoded
UA-CPU: x86
User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)
Host: 192.168.1.160
Content-Length: 357
Pragma: no-cache
Connection: keep-alive
Browser reload detected...
Posting 357 bytes...
Item=Value
Item2=Value+SecondToken+++
 FoldedInfo%0D%0A++MORE
Item+3=HelloHelloHello%0D%0A%0D%0A++++
    ++++

There really is no good spec on this.

URLFCHAR = Let's be forgiving.
A 	B 	C 	D 	E 	F 	G 	H 	I 	J 	K 	L 	M 	N 	O 	P 	Q 	R 	S 	T 	U 	V 	W 	X 	Y 	Z
a 	b 	c 	d 	e 	f 	g 	h 	i 	j 	k 	l 	m 	n 	o 	p 	q 	r 	s 	t 	u 	v 	w 	x 	y 	z
0 	1 	2 	3 	4 	5 	6 	7 	8 	9 	- 	_ 	. 	~
! 	* 	' 	( 	) 	; 	: 	@ 	$ 	, 	/ 	?  	# 	[ 	]

PERCENT = '%' for escape.
EQU = '=' for name/value separation.
PLUS = + for space replacement
AMP = & for separation
CR | LF = For item termination
WS = All other whitespace

Flags:
	!Done = if true, we are done.  Set after a terminal closure.

[START]
-> NULL->$Name
-> NULL->$Value
-> FALSE->!Done
-> NULL->$(Hex)Sixteens
-> [OPEN]
-> ^RETURN^

[OPEN]
	- URLFCHAR					- push, [NAME]
	- AMP						- burn
	- !OTHER!					- [DEPLETE], error(Query line started bad.  Must be an allowed character.)
	- CR						- [PENDING_LF]
	- !EOF!						- ^RETURN^		// Done.  Nothing to do.
	
[NAME]
	- %			- [ESCAPE]
	- WS		- [DEPLETE], error(broken name in query)
	- URLFCHAR	- push
	- +			- push(" ")	
	- EQU		- pop->$Name, [START_VALUE], if (!Done==TRUE) ^RETURN^
	- CR	 	- [PENDING_LF], [FOLDNAME_OPEN], if (!Done==TRUE) ^RETURN^			// We're coming out of a fold or line, so start a new name
	- !OTHER!	- [DEPLETE], error(bad characters in query name)
	- AMP		- error(Truncated query.  Name only.)	
	- !EOF!		- error(Truncated query.  Name only.)		
	
[FOLDNAME_OPEN]	
	- CR | LF 	- error(Name broken and without a value.)
	- WS		- [FOLDED_NAME], ^RETURN^
	- !OTHER!	- [DEPLETE], error(bad folding on name, lines aborted)
	- !EOF!		- error(Truncated query while folding name.)	

[FOLDED_NAME]
	- %			- [ESCAPE], [NAME], ^RETURN^				// return back to [NAME]
	- URLFCHAR	- push, [NAME],^RETURN^	
	- +			- push(" "), [NAME],^RETURN^	
	- EQU		- pop->$Name, [VALUE], ^RETURN^	
	- CR	 	- [PENDING_LF], [FOLDNAME_OPEN], ^RETURN^	// Recursion danger!
	- WS		- burn	
	- !OTHER!	- [DEPLETE], error(Bad folding on name, lines aborted)	
	- AMP		- error(Truncated query.  Name only.)	
	- !EOF!		- error(Truncated query while folding name.)		
	
[VALUE]
	- %			- [ESCAPE]	
	- URLFCHAR	- push
	- WS		- [DEPLETE], error(broken value in query)	
	- +			- push(" ")		
	- EQU		- [DEPLETE], error(Second unencoded '=' found in query.)	
	- AMP		- [SAVE], ^RETURN^ 
	- CR 	 	- [PENDING_LF], [FOLDVALUE_OPEN], ^RETURN^  			// Done, so unwind back to OPEN.
	- LF		- [FOLDVALUE_OPEN], ^RETURN^  							// Done, so unwind back to OPEN.
	- !OTHER!	- [DEPLETE], error(bad character in value)	
	- !EOF!		- [SAVE], ^RETURN^  									// Done, so unwind back to OPEN.	

[FOLDVALUE_OPEN]
	- %			- [SAVE], [ESCAPE], ^RETURN^  							// Closure.	 Push the char for the NEXT name.
	- +			- [SAVE], push(" "), ^RETURN^  							// Closure.	 Push the char for the NEXT name.		
	- URLFCHAR	- [SAVE], push,  ^RETURN^  								// Closure.  Push the char for the NEXT name.
	- AMP		- [SAVE], ^RETURN^ 										// Closure.  Back to open.
	- EQU		- [SAVE], error(Query entry started with a '='.)		// Closure but an error for the next line.	
	- CR	 	- [PENDING_LF],	[SAVE], [SEEK_MORE], ^RETURN^ 			// Closure.  Eat until we get characters.
	- !EOF!		- [SAVE], !Done=TRUE, ^RETURN^							// Absolute closure	
	- WS		- [FOLDED_VALUE], ^RETURN^   							// unwind back to NAME	
	- !OTHER!	- [DEPLETE], error(bad folding on value, lines aborted)
	- error(Truncated query while folding name.)	
	
[FOLDED_VALUE]
	- %			- [ESCAPE], [VALUE], ^RETURN^							// return back to [VALUE]
	- URLFCHAR	- push, [VALUE], ^RETURN^	
	- AMP		- [SAVE], ^RETURN^ 										// Closure.  Back to open.
	- +			- push(" "), [VALUE], ^RETURN^	
	- EQU		- [DEPLETE], error(Second unencoded '=' found in query.)	
	- CR	 	- [PENDING_LF], [FOLDVALUE_OPEN], ^RETURN^				// Recursion danger!
	- WS		- burn	
	- !OTHER!	- [DEPLETE], error(Bad folding on value, lines aborted)	
	- !EOF!		- [SAVE], !Done=TRUE, ^RETURN^							// Absolute Closure	
	
[SEEK_MORE]
	- %			- [ESCAPE], ^RETURN^				// Push the char for the NEXT name.
	- AMP		- burn, ^RETURN^ 					// Next item.
	- URLFCHAR	- push, ^RETURN^	 				// Push the char for the NEXT name.
	- +			- push(" "), ^RETURN^	 			// Push the char for the NEXT name.		
	- CR		- [PENDING_LF]						// Eat them
	- !EOF!		- !Done=TRUE, ^RETURN^				// We are already closed.  And now we are done.	
	- !OTHER!	- [DEPLETE], error(Bad next item in query or a broken fold.)		
	
[PENDING_LF]
	- LF		- ^RETURN^
	- !EOF!		- ^RETURN^ 		// Let this one slide.
	- !OTHER!	- [DEPLETE], error(broken CR/LF--missing LF)		
	
[SAVE]
-> pop->$Value
-> (Set request NV to $Name/$Value
-> ^RETURN^
	
[ESCAPE]
	- HEX		- ->$Sixteens, ESCAPEONES, ^RETURN^
	- !OTHER!	- error(broken escape)
	- !EOF!		- error(Truncated line with dangling escape.)			
	
[ESCAPEONES]
	- HEX		- push( ($SixteensSPLAT16)+HEX ), ^RETURN^
	- !OTHER!	- error(broken escape)	
	- !EOF!		- error(Truncated line with dangling escape.)			
	
[DEPLETE]
	- AMP		- burn, , ^RETURN^
	- CR		- [DEPLETE_CR], ^RETURN^
	- !OTHER!	- burn
	- !EOF!		- ^RETURN^  // So what.  Some browsers end abruptly.
	
[DEPLETE_CR]
	- LF		- ^RETURN^
	- !EOF!		- ^RETURN^  // So what.  Some browsers end abruptly.
	- !OTHER!	- fault(missing LF after CR at end of line: odd characters found, so stream is unreliable.)

[DRAIN]
	- LF		- burn, ^RETURN^
	- CR		- burn, ^RETURN^
	- !EOF!		- error(bad CR/LF line termination: truncated.)	
	- !OTHER!	- fault(bad CR/LF line termination: odd characters found, so stream is unreliable.)		
	
	</pre> 
	 * <p>
	 * @param reader source reader
	 * @param nv values
	 */
	synchronized public void parser(final Reader reader, final Map<String, String> nv) {
		
		// Prepare
		this.reader = reader;
		this.REG_nv = nv;
		
		// Invoke
		try {
			START();
		} catch (AnnotatedException ae) {
			throw ae;
		} catch (Exception e) {
			throw new AnnotatedException("Failed body parsing due general problem.", AnnotatedException.Catagory.FAULT, e);			
		}
	}
	
	// == REDUCTIONS ===================================================================
	
	/**
	 * [START]
-> NULL->$Name
-> NULL->$Value
-> FALSE->!Done
-> NULL->$(Hex)Sixteens
-> [OPEN]
-> ^RETURN^
	 */
	private void START() throws Exception {
		REG_name = null;
		REG_value = null;
		// REG_request = null; // We don't do this.
		REG_sixteens = 0;
		FLAG_Done = false;
		ACCUMULATOR = new StringBuffer();
		OPEN();		
	}
	
	/**
	 * [OPEN]
		- URLFCHAR					- push, [NAME]
		- AMP						- burn	
		- !OTHER!					- [DEPLETE], error(Query line started bad.  Must be an allowed character.)
		- CR						- [PENDING_LF]
		- !EOF!						- ^RETURN^		// Done.  Nothing to do.
	 */
	private void OPEN() throws Exception {
		int character = reader.read();
		while (character >= 0) {
			switch(CharacterTool.getURLFType(character)) {			
			
			case URLFCHAR:
				ACCUMULATOR.append((char)character); 
				NAME();
				break;

			case WS_CR_CONTROL:
				PENDING_LF();
				break;

			case SPECIAL_AMP:
				break;
				
			default:
				DEPLETE();
				error("Query line started bad.  Must be an allowed character..  character=" + character);
			break;						
				
			} // end switch
			character = reader.read();
		} // end while
		
	} // END OPEN()
	
	/**
[NAME]
	- %			- [ESCAPE]
	- WS		- [DEPLETE], error(broken name in query)
	- URLFCHAR	- push
	- +			- push(" ")	
	- EQU		- pop->$Name, [START_VALUE], if (!Done==TRUE) ^RETURN^
	- CR	 	- [PENDING_LF], [FOLDNAME_OPEN], if (!Done==TRUE) ^RETURN^			// We're coming out of a fold or line, so start a new name
	- !OTHER!	- [DEPLETE], error(bad characters in query name)
	- AMP		- error(Truncated query.  Name only.)		
	- !EOF!		- error(Truncated query.  Name only.)	
	 */
	private void NAME() throws Exception {
		int character = reader.read();
		while (character >= 0) {
			switch(CharacterTool.getURLFType(character)) {			
			
			case URLFCHAR:
				ACCUMULATOR.append((char)character); 
				break;
				
			case SPECIAL_PLUS:
				ACCUMULATOR.append(' '); 
				break;				
				
			case SPECIAL_PERCENT:
				ESCAPE(); 
				break;
				
			case WS:			
				DEPLETE();
				error("Broken name in query.");
				break;
				
			case SPECIAL_EQ:		
				REG_name = pop();
				VALUE();
				if (FLAG_Done == true) return;
				break;
				
			case WS_CR_CONTROL:
				PENDING_LF();
				FOLDNAME_OPEN();
				if (FLAG_Done == true) return;				
				break;

			case SPECIAL_AMP:
			default:
				DEPLETE();
				error("Bad characters in query name.  character=" + character);
			break;						
				
			} // end switch
			character = reader.read();
		} // end while
		
		// !EOF!
		error("Truncated query.  Name only.");
		
	} // end NAME
	
	/**
[FOLDNAME_OPEN]	
	- CR | LF 	- error(Name broken and without a value.)
	- WS		- [FOLDED_NAME], ^RETURN^
	- !OTHER!	- [DEPLETE], error(bad folding on name, lines aborted)
	- !EOF!		- error(Truncated query while folding name.)	
	 */
	private void FOLDNAME_OPEN() throws Exception {
		int character = reader.read();
		while (character >= 0) {
			switch(CharacterTool.getURLFType(character)) {			
			
			case WS:
				FOLDED_NAME();
				return;
	
			case WS_CR_CONTROL:
			case WS_LF_CONTROL:
				error("Name broken and without a value.");
				break;
	
			default:
				DEPLETE();
				error("Bad folding on name, lines aborted  character=" + character);
			break;						
				
			} // end switch
			character = reader.read();
		} // end while
		
		// !EOF!
		error("Truncated query while folding name.");
		
	} // END FOLDNAME_OPEN()
			
	/**
[FOLDED_NAME]
	- %			- [ESCAPE], [NAME], ^RETURN^				// return back to [NAME]
	- URLFCHAR	- push, [NAME],^RETURN^	
	- +			- push(" "), [NAME],^RETURN^	
	- EQU		- pop->$Name, [VALUE], ^RETURN^	
	- CR	 	- [PENDING_LF], [FOLDNAME_OPEN], ^RETURN^	// Recursion danger!
	- WS		- burn	
	- !OTHER!	- [DEPLETE], error(Bad folding on name, lines aborted)	
	- AMP		- error(Truncated query.  Name only.)		
	- !EOF!		- error(Truncated query while folding name.)	
		 */
		private void FOLDED_NAME() throws Exception {
			int character = reader.read();
			while (character >= 0) {
				switch(CharacterTool.getURLFType(character)) {			
				
				case SPECIAL_PERCENT:
					ESCAPE(); 
					NAME();
					return;
				
				case URLFCHAR:
					ACCUMULATOR.append((char)character); 
					NAME();
					return;
					
				case SPECIAL_PLUS:
					ACCUMULATOR.append(' '); 
					NAME();
					return;			
							
				case SPECIAL_EQ:		
					REG_name = pop();
					VALUE();
					return;
					
				case WS_CR_CONTROL:
					PENDING_LF();
					FOLDNAME_OPEN();
					return;				
					
				case WS:			
					break;
					
				case SPECIAL_AMP:
					DEPLETE();
					error("Truncated query while folding name.");					
					
				default:
					DEPLETE();
					error("Bad folding on name, lines aborted.  character=" + character);
				break;						
					
				} // end switch
				character = reader.read();
			} // end while
			
			// !EOF!
			error("Truncated query while folding name.");
			
		} // end FOLDED_NAME	
	
		/**
[VALUE]
	- %			- [ESCAPE]	
	- URLFCHAR	- push
	- WS		- [DEPLETE], error(broken value in query)	
	- +			- push(" ")		
	- EQU		- [DEPLETE], error(Second unencoded '=' found in query.)	
	- AMP		- [SAVE], ^RETURN^ 	
	- CR 	 	- [PENDING_LF], [FOLDVALUE_OPEN], ^RETURN^  			// Done, so unwind back to OPEN.
	- LF		- [FOLDVALUE_OPEN], ^RETURN^  							// Done, so unwind back to OPEN.
	- !OTHER!	- [DEPLETE], error(bad character in value)	
	- !EOF!		- [SAVE], !Done=TRUE, ^RETURN^  									// Done, so unwind back to OPEN.	
			*/
	private void VALUE() throws Exception {
		int character = reader.read();
		while (character >= 0) {	
			switch(CharacterTool.getURLFType(character)) {			
			
			case SPECIAL_PERCENT:
				ESCAPE(); 
				break;
			
			case URLFCHAR:
				ACCUMULATOR.append((char)character); 
				break;
				
			case WS:		
				DEPLETE();
				error("Broken value in query.");
				break;			
				
			case SPECIAL_PLUS:
				ACCUMULATOR.append(' '); 
				break;			
						
			case SPECIAL_EQ:		
				DEPLETE();
				error("Second unencoded '=' found in query.");
				break;
				
			case SPECIAL_AMP:
				SAVE();
				return;
				
			case WS_CR_CONTROL:
				PENDING_LF();
				FOLDVALUE_OPEN();
				return;			
				
			case WS_LF_CONTROL:				
				FOLDVALUE_OPEN();
				return;			
				
			default:
				DEPLETE();
				error("Bad character in value.  character=" + character);
				break;						
				
			} // end switch
			character = reader.read();
		} // end while
		
		// !EOF!
		SAVE();
		FLAG_Done = true;
		return;
		
	} // end VALUE	
			
	/**
[FOLDVALUE_OPEN]
	- %			- [SAVE], [ESCAPE], ^RETURN^  							// Closure.	 Push the char for the NEXT name.
	- +			- [SAVE], push(" "), ^RETURN^  							// Closure.	 Push the char for the NEXT name.		
	- URLFCHAR	- [SAVE], push,  ^RETURN^  								// Closure.  Push the char for the NEXT name.
	- AMP		- [SAVE], ^RETURN^ 										// Closure.  Back to open.
	- EQU		- [SAVE], error(Query entry started with a '='.)		// Closure but an error for the next line.	
	- CR	 	- [PENDING_LF],	[SAVE], [SEEK_MORE], ^RETURN^ 			// Closure.  Eat until we get characters.
	- !EOF!		- [SAVE], !Done=TRUE, ^RETURN^							// Absolute closure	
	- WS		- [FOLDED_VALUE], ^RETURN^   							// unwind back to NAME	
	- !OTHER!	- [DEPLETE], error(bad folding on value, lines aborted)
	- error(Truncated query while folding name.)	
			 */
	private void FOLDVALUE_OPEN() throws Exception {
		int character = reader.read();
		while (character >= 0) {	
			
			switch(CharacterTool.getURLFType(character)) {			
			
			case SPECIAL_PERCENT:
				SAVE();
				ESCAPE(); 
				return;
			
			case SPECIAL_PLUS:
				SAVE();
				ACCUMULATOR.append(' '); 
				return;		
				
			case URLFCHAR:
				SAVE();
				ACCUMULATOR.append((char)character); 
				return;
							
			case SPECIAL_AMP:
				SAVE();
				return;
				
			case SPECIAL_EQ:		
				SAVE();
				error("Query entry started with a '='.");
				return;
				
			case WS_CR_CONTROL:
				PENDING_LF();
				SAVE();
				SEEK_MORE();
				return;				
				
			case WS:
				FOLDED_VALUE();
				return;
				
			default:
				DEPLETE();
				error("Bad folding on value, lines aborted.  character=" + character);
			break;						
				
			} // end switch
			character = reader.read();	
		} // end while
		
		// !EOF!
		SAVE();
		FLAG_Done = true;
		return;
		
	} // end FOLDVALUE_OPEN	
			
	/**
[FOLDED_VALUE]
	- %			- [ESCAPE], [VALUE], ^RETURN^				// return back to [VALUE]
	- URLFCHAR	- push, [VALUE],^RETURN^	
	- +			- push(" "), [VALUE],^RETURN^	
	- EQU		- [DEPLETE], error(Second unencoded '=' found in query.)	
	- AMP		- [SAVE], ^RETURN^ 										// Closure.  Back to open.	
	- CR	 	- [PENDING_LF], [FOLDVALUE_OPEN], ^RETURN^	// Recursion danger!
	- WS		- burn	
	- !OTHER!	- [DEPLETE], error(Bad folding on value, lines aborted)	
	- !EOF!		- [SAVE], !Done=TRUE, ^RETURN^	
				 */
	private void FOLDED_VALUE() throws Exception {
		int character = reader.read();
		while (character>=0) {
			switch(CharacterTool.getURLFType(character)) {			
			
			case SPECIAL_PERCENT:
				SAVE();
				ESCAPE(); 
				return;
			
			case SPECIAL_PLUS:
				SAVE();
				ACCUMULATOR.append(' '); 
				return;		
				
			case URLFCHAR:
				SAVE();
				ACCUMULATOR.append((char)character); 
				return;
							
			case SPECIAL_AMP:
				SAVE();
				return;
								
			case SPECIAL_EQ:		
				SAVE();
				error("Query entry started with a '='.");
				return;
				
			case WS_CR_CONTROL:
				PENDING_LF();
				SAVE();
				SEEK_MORE();
				return;				
				
			case WS:
				FOLDED_VALUE();
				return;
				
			default:
				DEPLETE();
				error("Bad folding on value, lines aborted.  character=" + character);
			break;						
				
			} // end switch
			character = reader.read();
		} // end while
		
		// !EOF!
		SAVE();
		FLAG_Done = true;
		return;
		
	} // end FOLDED_VALUE		
	
	/**
[SEEK_MORE]
	- %			- [ESCAPE], ^RETURN^				// Push the char for the NEXT name.
	- URLFCHAR	- push, ^RETURN^	 				// Push the char for the NEXT name.
	- AMP		- burn, ^RETURN^ 					// Next item.	
	- +			- push(" "), ^RETURN^	 			// Push the char for the NEXT name.	
	- CR		- [PENDING_LF]						// Eat them
	- !EOF!		- !Done=TRUE, ^RETURN^				// We are already closed.  And now we are done.	
	- !OTHER!	- [DEPLETE], error(Bad next item in query or a broken fold.)	
					 */
	private void SEEK_MORE() throws Exception {
		int character = reader.read();
		while (character >= 0) {
			switch(CharacterTool.getURLFType(character)) {			
			
			case SPECIAL_PERCENT:
				ESCAPE(); 
				return;
			
			case SPECIAL_PLUS:
				SAVE();
				ACCUMULATOR.append(' '); 
				return;		
				
			case SPECIAL_AMP:
				return;
				
			case URLFCHAR:
				ACCUMULATOR.append((char)character); 
				return;
				
			case WS_CR_CONTROL:
				PENDING_LF();
				break;		
				
			default:
				DEPLETE();
				error("Bad next item in query or a broken fold.  character=" + character);
			break;						
				
			} // end switch
			character = reader.read();	
		} // end while
		
		// !EOF!
		this.FLAG_Done = true;
		return;
		
	} // end SEEK_MORE		
		
	
	/**
[PENDING_LF]
	- LF		- ^RETURN^
	- !EOF!		- ^RETURN^ 		// Let this one slide.
	- !OTHER!	- [DEPLETE], error(broken CR/LF--missing LF)			
	 * @throws Throwable
	 */
	private void PENDING_LF() throws Exception {
		if (true) {
			if (CharacterTool.getURLFType(reader.read()) != WS_LF_CONTROL) {
				DEPLETE();
				error("error(broken CR/LF--missing LF)");
			}
		}
		
	} // end PENDING_LF		
	
	
	/**
	[SAVE]
	-> pop->$Value
	-> (Set request NV to $Name/$Value
	-> ^RETURN^
	 * @throws Throwable
	 */
	private void SAVE() throws Exception {
		REG_value = pop();
		REG_nv.put(this.REG_name, REG_value);
		
	} // end SAVE	
		
	/**
	 * [ESCAPE]
	- (HEX)		- ->$Sixteens, ESCAPEONES, ^RETURN^
	- !OTHER!	- error(broken escape)
	- !EOF!		- error(Truncated line with dangling escape.)			
	 * @throws Throwable
	 */
	private void ESCAPE() throws Exception {
		
		REG_sixteens = CharacterTool.getHexValue(reader.read());
		if (REG_sixteens < 0) error("Broken escape.");
		else {
			ESCAPEONES();
			return;
		}
		
	} // end ESCAPE	
	
	/**
	 * [ESCAPEONES]
	- HEX		- push( ($SixteensSPLAT16)PLUSHEX ), ^RETURN^
	- !OTHER!	- error(broken escape)	
	- !EOF!		- error(Truncated line with dangling escape.)		
	 * @throws Throwable
	 */
	private void ESCAPEONES() throws Exception {
		//if (!true) error("Truncated line with dangling escape.");
		
		int ones = CharacterTool.getHexValue(reader.read());
		if (ones < 0) error("BTruncated line with dangling escape.");
		else {
			ACCUMULATOR.append((char) ((REG_sixteens * 16)+ones) ); 
			return;
		}
		
	} // end ESCAPEONES	
	
	/**
	 * [DEPLETE]
	- AMP		- burn, , ^RETURN^	 * 
	- CR		- [DEPLETE_CR], ^RETURN^
	- !OTHER!	- burn
	- !EOF!		- ^RETURN^  // So what.  Some browsers end abruptly.
	 * @throws Throwable
	 */
	private void DEPLETE() throws Exception {
		int character = reader.read();
		while (character >= 0) {
			
			switch(CharacterTool.getURLFType(character)) {		
			case WS_CR_CONTROL:
				DEPLETE_CR();
				return;
				
			case SPECIAL_AMP:
				return;
				
			default:
				// ELSE BURN IT
				break;
			
			} //end case
			character = reader.read();	
		} // end while
		// !EOF!
		
	} // end VERSION
	
	/**
	 * [DEPLETE_CR]
	- LF		- [CR], ^RETURN^
	- !EOF!		- ^RETURN^  // So what.  Some browsers end abruptly.
	- !OTHER!	- fault(missing LF after CR at end of line: odd characters found, so stream is unreliable.)
	 * @throws Throwable
	 */
	private void DEPLETE_CR() throws Exception {
		//if (!true) return;
		
		if (reader.read() == WS_LF_CONTROL) {
			return;
		} 
		
		fault("Missing LF after CR at end of line: odd characters found, so stream is unreliable.");
		
	} // end DEPLETE_CR	
	
	// == TOOLS ===================================================================
	
	private void error(final String text) {
		AnnotatedException ae = new AnnotatedException("Error while parsing form.", AnnotatedException.Catagory.ERROR)
				.annotate("message", text);
		if (ACCUMULATOR != null) {
			ae.annotate("processes.text", ACCUMULATOR.toString());
		} 
		throw ae;
	}
	
	/**
	 * Throw a fault with consistent formatting and the code GENERAL_PARSER_FAULT.
	 * @param text the text of the error.
	 * @throws Throwable
	 */
	private void fault(final String text) {
		AnnotatedException ae = new AnnotatedException("Fault while parsing form.", AnnotatedException.Catagory.FAULT)
				.annotate("message", text);
		if (ACCUMULATOR != null) {
			ae.annotate("processes.text", ACCUMULATOR.toString());
		} 
		throw ae;
	}


	/**
	 * Pop the accumulator.  The accumulator will be empty when this is done.
	 * @throws Throwable
	 * @return the contents of the accumulator.
	 */
	private String pop() throws Exception {
		String result = ACCUMULATOR.toString();
		ACCUMULATOR = new StringBuffer();
		return result;
	}
	
}


