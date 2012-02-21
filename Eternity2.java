import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Eternity2 {

    private ConcurrentHashMap<Integer, Piece> piecesNormal, placedPieces, piecesCorner, piecesOuter;
    private int[] placedGrid, rotationGrid, moveList;
    int lastAddedMove = 0, recordMove = 0;

    public Eternity2() {
	placedGrid = new int[256];
	rotationGrid = new int[256];
    }

    /**
     * Place a piece on the grid (board).
     *
     * @param pieceNumber Piece number (located on back of real piece).
     * @param boardLocation Grid number (location on the "board").
     * @param rotation Rotation steps of piece clockwise.
     * @param recursionDepth Depth of recursion.
     */
    public void placePiece(int pieceNumber, int boardLocation, int rotation, int recursionDepth) {
	Piece p = pieceMap(recursionDepth).remove(pieceNumber);
	p.setRotation(rotation);
	placedPieces.put(pieceNumber, p);
	placedGrid[boardLocation] = pieceNumber;
	rotationGrid[boardLocation] = rotation;
    }

    /**
     * Remove a piece from the grid (board).
     * 
     * @param pieceNumber Piece number (located on back of real piece).
     * @param recursionDepth Depth of recursion.
     */
    public void removePiece(int pieceNumber, int recursionDepth) {
	pieceMap(recursionDepth).put(placedGrid[pieceNumber], placedPieces.remove(placedGrid[pieceNumber]));
	placedGrid[pieceNumber] = 0;
	rotationGrid[pieceNumber] = 0;
    }

    /**
     * Move to next step and try to solve the board.
     * 
     * @param recursionDepth Depth of recursion.
     * @return Valid solution for puzzle or data corruption.
     */
    public boolean nextMove(int recrusionDepth) {
	if (recordMove <= recrusionDepth) {
	    recordMove = recrusionDepth;
	    log();
	}

	if (moveList.length == recrusionDepth) {
	    return true;
	}

	String searchString = "";
	int gridPosition = moveList[recrusionDepth];
	int gridCheck;

	if (recrusionDepth < 224 || recrusionDepth > 239) {
	    gridCheck = placedGrid[gridPosition - 16];
	    if (gridCheck != 0) {
		searchString += ((Piece) placedPieces.get(gridCheck)).get(2);
	    } else {
		searchString += " ";
	    }
	} else {
	    searchString += "X";
	}

	if (recrusionDepth < 239) {
	    gridCheck = placedGrid[gridPosition + 1];
	    if (gridCheck != 0) {
		searchString += ((Piece) placedPieces.get(gridCheck)).get(3);
	    } else {
		searchString += " ";
	    }
	} else {
	    searchString += "X";
	}

	if (recrusionDepth < 195 || (recrusionDepth > 209 && recrusionDepth < 254)) {
	    gridCheck = placedGrid[gridPosition + 16];
	    if (gridCheck != 0) {
		searchString += ((Piece) placedPieces.get(gridCheck)).get(0);
	    } else {
		searchString += " ";
	    }
	} else {
	    searchString += "X";
	}

	if (recrusionDepth < 209 || recrusionDepth > 224) {
	    gridCheck = placedGrid[gridPosition - 1];
	    if (gridCheck != 0) {
		searchString += ((Piece) placedPieces.get(gridCheck)).get(1);
	    } else {
		searchString += " ";
	    }
	} else {
	    searchString += "X";
	}

	int startIndex = -1;
	int stopIndex = -1;
	int count = 0;

	searchString = searchString + searchString;

	for (int i = 0; i < 8; i++) {
	    if (count == 0 && searchString.substring(i, i + 1).equals(" ")) {
		count = 1;
	    }
	    if (count == 1 && !searchString.substring(i, i + 1).equals(" ") && startIndex == -1) {
		startIndex = i;
		count = 2;
	    }
	    if (count == 2 && searchString.substring(i, i + 1).equals(" ") && stopIndex == -1) {
		stopIndex = i;
	    }
	}

	if (startIndex != -1) {
	    searchString = searchString.substring(startIndex, stopIndex);
	} else {
	    searchString = searchString.substring(0, 4);
	    startIndex = 0;
	}

	for (Entry<Integer, Piece> piece : pieceSet(recrusionDepth)) {
	    HashMap<Integer, String> searchResult = piece.getValue().find(searchString);
	    if (searchResult.size() > 0) {
		for (Integer pieceRotation : searchResult.keySet()) {
		    int rotate = (startIndex - pieceRotation < 0) ? 4 + startIndex - pieceRotation : startIndex - pieceRotation;
		    placePiece(piece.getKey(), gridPosition, rotate, recrusionDepth);
		    if (nextMove(recrusionDepth + 1)) {
			return true;
		    }
		    removePiece(gridPosition, recrusionDepth);
		}
	    }
	}

	return false;
    }

    /**
     * Get set of pieces depending on where on the grid (board) the
     * puzzle are currently at.
     * 
     * @param position Grid position.
     * @return Set of pieces.
     */
    public Set<Entry<Integer, Piece>> pieceSet(int position) {
	return pieceMap(position).entrySet();
    }

    /**
     * Get Map of pieces and their numbers depending on where on the grid
     * (board) the puzzle are currently at.
     * 
     * @param position Grid position.
     * @return  Map of pieces.
     */
    public ConcurrentHashMap<Integer, Piece> pieceMap(int position) {
	if (position == 209 || position == 224 || position == 239 || position == 254) {
	    return piecesCorner;
	} else if (position >= 195) {
	    return piecesOuter;
	} else {
	    return piecesNormal;
	}
    }
    
    /**
     * Print current board to terminal.
     */
    public void log() {
	StringBuilder sb = new StringBuilder();
	sb.append(new Date(System.currentTimeMillis()));
	sb.append(" - New Record or record variation!\n");
	for (int i = 1; i < 257; i++) {
	    String pieceString = "  " + placedGrid[i - 1];
	    sb.append("[");
	    sb.append(pieceString.substring(pieceString.length() - 3, pieceString.length()));
	    sb.append("(");
	    sb.append(rotationGrid[i - 1]);
	    sb.append("] ");
	    if (i % 16 == 0) {
		sb.append("\n");
	    }
	}
	sb.append("Pieces: ");
	sb.append(recordMove);
	sb.append(".\n\n");
	
	System.out.println(sb);
	writeToFile(sb.toString());
    }
    
     /**
     * Write message to file.
     * 
     * @param message String to write to file.
     */
    protected void writeToFile(String message) {
	PrintWriter out = null;
	try {
	    FileWriter fstream = new FileWriter("puzzleLog.txt", true);
	    out = new PrintWriter(fstream);
	    out.println(message);
	    out.flush();
	} catch (Exception e) {
	    System.out.println("Unable to write log file.");
	} finally {
	    if (out != null) {
		out.close();
	    }
	}
    }

    /**
     * Load default set of pieces.
     */
    public void loadPieces() {
	piecesNormal = new ConcurrentHashMap<Integer, Piece>();
	placedPieces = new ConcurrentHashMap<Integer, Piece>();
	piecesCorner = new ConcurrentHashMap<Integer, Piece>();
	piecesOuter = new ConcurrentHashMap<Integer, Piece>();

	piecesCorner.put(1, new Piece("AQXX"));
	piecesCorner.put(2, new Piece("AEXX"));
	piecesCorner.put(3, new Piece("IQXX"));
	piecesCorner.put(4, new Piece("QIXX"));

	piecesOuter.put(5, new Piece("BAXA"));
	piecesOuter.put(6, new Piece("JIXA"));
	piecesOuter.put(7, new Piece("FAXA"));
	piecesOuter.put(8, new Piece("FMXA"));
	piecesOuter.put(9, new Piece("KQXA"));
	piecesOuter.put(10, new Piece("GEXA"));
	piecesOuter.put(11, new Piece("OIXA"));
	piecesOuter.put(12, new Piece("HEXA"));
	piecesOuter.put(13, new Piece("HMXA"));
	piecesOuter.put(14, new Piece("UEXA"));
	piecesOuter.put(15, new Piece("JAXI"));
	piecesOuter.put(16, new Piece("RQXI"));
	piecesOuter.put(17, new Piece("NMXI"));
	piecesOuter.put(18, new Piece("SMXI"));
	piecesOuter.put(19, new Piece("GIXI"));
	piecesOuter.put(20, new Piece("OIXI"));
	piecesOuter.put(21, new Piece("DEXI"));
	piecesOuter.put(22, new Piece("LAXI"));
	piecesOuter.put(23, new Piece("LMXI"));
	piecesOuter.put(24, new Piece("TAXI"));
	piecesOuter.put(25, new Piece("UAXI"));
	piecesOuter.put(26, new Piece("BIXQ"));
	piecesOuter.put(27, new Piece("BQXQ"));
	piecesOuter.put(28, new Piece("JQXQ"));
	piecesOuter.put(29, new Piece("RQXQ"));
	piecesOuter.put(30, new Piece("GMXQ"));
	piecesOuter.put(31, new Piece("OIXQ"));
	piecesOuter.put(32, new Piece("TQXQ"));
	piecesOuter.put(33, new Piece("HIXQ"));
	piecesOuter.put(34, new Piece("HEXQ"));
	piecesOuter.put(35, new Piece("PMXQ"));
	piecesOuter.put(36, new Piece("VEXQ"));
	piecesOuter.put(37, new Piece("RAXE"));
	piecesOuter.put(38, new Piece("CMXE"));
	piecesOuter.put(39, new Piece("KMXE"));
	piecesOuter.put(40, new Piece("SIXE"));
	piecesOuter.put(41, new Piece("SQXE"));
	piecesOuter.put(42, new Piece("OAXE"));
	piecesOuter.put(43, new Piece("OIXE"));
	piecesOuter.put(44, new Piece("OQXE"));
	piecesOuter.put(45, new Piece("DAXE"));
	piecesOuter.put(46, new Piece("TEXE"));
	piecesOuter.put(47, new Piece("HEXE"));
	piecesOuter.put(48, new Piece("PEXE"));
	piecesOuter.put(49, new Piece("BMXM"));
	piecesOuter.put(50, new Piece("JAXM"));
	piecesOuter.put(51, new Piece("JIXM"));
	piecesOuter.put(52, new Piece("FAXM"));
	piecesOuter.put(53, new Piece("GEXM"));
	piecesOuter.put(54, new Piece("DEXM"));
	piecesOuter.put(55, new Piece("DMXM"));
	piecesOuter.put(56, new Piece("HQXM"));
	piecesOuter.put(57, new Piece("PAXM"));
	piecesOuter.put(58, new Piece("PMXM"));
	piecesOuter.put(59, new Piece("UIXM"));
	piecesOuter.put(60, new Piece("VQXM"));

	piecesNormal.put(61, new Piece("FRBB"));
	piecesNormal.put(62, new Piece("NGBB"));
	piecesNormal.put(63, new Piece("JCBJ"));
	piecesNormal.put(64, new Piece("BHBR"));
	piecesNormal.put(65, new Piece("RVBR"));
	piecesNormal.put(66, new Piece("NNBR"));
	piecesNormal.put(67, new Piece("KJBR"));
	piecesNormal.put(68, new Piece("TFBR"));
	piecesNormal.put(69, new Piece("VHBR"));
	piecesNormal.put(70, new Piece("CGBC"));
	piecesNormal.put(71, new Piece("GLBC"));
	piecesNormal.put(72, new Piece("NRBK"));
	piecesNormal.put(73, new Piece("ODBK"));
	piecesNormal.put(74, new Piece("TOBK"));
	piecesNormal.put(75, new Piece("HCBK"));
	piecesNormal.put(76, new Piece("NOBS"));
	piecesNormal.put(77, new Piece("SOBS"));
	piecesNormal.put(78, new Piece("CPBG"));
	piecesNormal.put(79, new Piece("TCBG"));
	piecesNormal.put(80, new Piece("PUBG"));
	piecesNormal.put(81, new Piece("SRBO"));
	piecesNormal.put(82, new Piece("RRBD"));
	piecesNormal.put(83, new Piece("KDBD"));
	piecesNormal.put(84, new Piece("RSBL"));
	piecesNormal.put(85, new Piece("FNBL"));
	piecesNormal.put(86, new Piece("HLBL"));
	piecesNormal.put(87, new Piece("PTBL"));
	piecesNormal.put(88, new Piece("BUBT"));
	piecesNormal.put(89, new Piece("FVBT"));
	piecesNormal.put(90, new Piece("DPBT"));
	piecesNormal.put(91, new Piece("KLBH"));
	piecesNormal.put(92, new Piece("SOBH"));
	piecesNormal.put(93, new Piece("SDBH"));
	piecesNormal.put(94, new Piece("DUBH"));
	piecesNormal.put(95, new Piece("LNBH"));
	piecesNormal.put(96, new Piece("UCBU"));
	piecesNormal.put(97, new Piece("DSBV"));
	piecesNormal.put(98, new Piece("THBV"));
	piecesNormal.put(99, new Piece("UFBV"));
	piecesNormal.put(100, new Piece("VUBV"));
	piecesNormal.put(101, new Piece("LOJJ"));
	piecesNormal.put(102, new Piece("LPJJ"));
	piecesNormal.put(103, new Piece("PSJJ"));
	piecesNormal.put(104, new Piece("VFJJ"));
	piecesNormal.put(105, new Piece("DOJR"));
	piecesNormal.put(106, new Piece("CHJF"));
	piecesNormal.put(107, new Piece("SHJF"));
	piecesNormal.put(108, new Piece("DOJF"));
	piecesNormal.put(109, new Piece("PKJF"));
	piecesNormal.put(110, new Piece("OLJN"));
	piecesNormal.put(111, new Piece("LOJN"));
	piecesNormal.put(112, new Piece("TSJC"));
	piecesNormal.put(113, new Piece("TPJC"));
	piecesNormal.put(114, new Piece("NDJK"));
	piecesNormal.put(115, new Piece("GLJK"));
	piecesNormal.put(116, new Piece("LKJK"));
	piecesNormal.put(117, new Piece("VPJK"));
	piecesNormal.put(118, new Piece("CUJS"));
	piecesNormal.put(119, new Piece("PLJG"));
	piecesNormal.put(120, new Piece("HVJO"));
	piecesNormal.put(121, new Piece("NVJD"));
	piecesNormal.put(122, new Piece("FPJT"));
	piecesNormal.put(123, new Piece("NSJT"));
	piecesNormal.put(124, new Piece("TOJT"));
	piecesNormal.put(125, new Piece("LVJH"));
	piecesNormal.put(126, new Piece("UOJH"));
	piecesNormal.put(127, new Piece("NFJP"));
	piecesNormal.put(128, new Piece("SUJP"));
	piecesNormal.put(129, new Piece("DCJP"));
	piecesNormal.put(130, new Piece("THJP"));
	piecesNormal.put(131, new Piece("FTJU"));
	piecesNormal.put(132, new Piece("LNJU"));
	piecesNormal.put(133, new Piece("NPJV"));
	piecesNormal.put(134, new Piece("KDJV"));
	piecesNormal.put(135, new Piece("DCJV"));
	piecesNormal.put(136, new Piece("PTJV"));
	piecesNormal.put(137, new Piece("TGRR"));
	piecesNormal.put(138, new Piece("FCRF"));
	piecesNormal.put(139, new Piece("FKRF")); // Start piece
	piecesNormal.put(140, new Piece("FLRF"));
	piecesNormal.put(141, new Piece("SURF"));
	piecesNormal.put(142, new Piece("OFRF"));
	piecesNormal.put(143, new Piece("PLRF"));
	piecesNormal.put(144, new Piece("UURF"));
	piecesNormal.put(145, new Piece("CDRN"));
	piecesNormal.put(146, new Piece("RLRC"));
	piecesNormal.put(147, new Piece("RVRC"));
	piecesNormal.put(148, new Piece("CNRC"));
	piecesNormal.put(149, new Piece("OLRC"));
	piecesNormal.put(150, new Piece("FKRS"));
	piecesNormal.put(151, new Piece("DVRS"));
	piecesNormal.put(152, new Piece("KKRG"));
	piecesNormal.put(153, new Piece("KSRG"));
	piecesNormal.put(154, new Piece("VPRG"));
	piecesNormal.put(155, new Piece("GGRD"));
	piecesNormal.put(156, new Piece("GLRD"));
	piecesNormal.put(157, new Piece("VGRD"));
	piecesNormal.put(158, new Piece("GPRT"));
	piecesNormal.put(159, new Piece("HFRT"));
	piecesNormal.put(160, new Piece("UURH"));
	piecesNormal.put(161, new Piece("FTRP"));
	piecesNormal.put(162, new Piece("NTRP"));
	piecesNormal.put(163, new Piece("OKRV"));
	piecesNormal.put(164, new Piece("DPRV"));
	piecesNormal.put(165, new Piece("CDFN"));
	piecesNormal.put(166, new Piece("DHFN"));
	piecesNormal.put(167, new Piece("CCFK"));
	piecesNormal.put(168, new Piece("KOFS"));
	piecesNormal.put(169, new Piece("SUFS"));
	piecesNormal.put(170, new Piece("DHFG"));
	piecesNormal.put(171, new Piece("TPFG"));
	piecesNormal.put(172, new Piece("UKFG"));
	piecesNormal.put(173, new Piece("OOFO"));
	piecesNormal.put(174, new Piece("LTFO"));
	piecesNormal.put(175, new Piece("GUFD"));
	piecesNormal.put(176, new Piece("GSFL"));
	piecesNormal.put(177, new Piece("NDFT"));
	piecesNormal.put(178, new Piece("LPFH"));
	piecesNormal.put(179, new Piece("HOFH"));
	piecesNormal.put(180, new Piece("GPFP"));
	piecesNormal.put(181, new Piece("KPFU"));
	piecesNormal.put(182, new Piece("GKFU"));
	piecesNormal.put(183, new Piece("SHNN"));
	piecesNormal.put(184, new Piece("VGNC"));
	piecesNormal.put(185, new Piece("SLNK"));
	piecesNormal.put(186, new Piece("HHNK"));
	piecesNormal.put(187, new Piece("UGNS"));
	piecesNormal.put(188, new Piece("NUNG"));
	piecesNormal.put(189, new Piece("CSNG"));
	piecesNormal.put(190, new Piece("PSNG"));
	piecesNormal.put(191, new Piece("CCNO"));
	piecesNormal.put(192, new Piece("OTNO"));
	piecesNormal.put(193, new Piece("KGND"));
	piecesNormal.put(194, new Piece("UKNL"));
	piecesNormal.put(195, new Piece("UVNL"));
	piecesNormal.put(196, new Piece("VONL"));
	piecesNormal.put(197, new Piece("KVNT"));
	piecesNormal.put(198, new Piece("SHNT"));
	piecesNormal.put(199, new Piece("TTNT"));
	piecesNormal.put(200, new Piece("SCNH"));
	piecesNormal.put(201, new Piece("UHNP"));
	piecesNormal.put(202, new Piece("VGNP"));
	piecesNormal.put(203, new Piece("LSNU"));
	piecesNormal.put(204, new Piece("LHNU"));
	piecesNormal.put(205, new Piece("PCNU"));
	piecesNormal.put(206, new Piece("VUNU"));
	piecesNormal.put(207, new Piece("VGCC"));
	piecesNormal.put(208, new Piece("SVCK"));
	piecesNormal.put(209, new Piece("HOCK"));
	piecesNormal.put(210, new Piece("KSCG"));
	piecesNormal.put(211, new Piece("POCG"));
	piecesNormal.put(212, new Piece("CPCO"));
	piecesNormal.put(213, new Piece("HHCD"));
	piecesNormal.put(214, new Piece("CTCL"));
	piecesNormal.put(215, new Piece("DVCL"));
	piecesNormal.put(216, new Piece("VUCL"));
	piecesNormal.put(217, new Piece("SOCT"));
	piecesNormal.put(218, new Piece("DLCP"));
	piecesNormal.put(219, new Piece("KDCU"));
	piecesNormal.put(220, new Piece("KPCV"));
	piecesNormal.put(221, new Piece("UUCV"));
	piecesNormal.put(222, new Piece("UVCV"));
	piecesNormal.put(223, new Piece("LVKK"));
	piecesNormal.put(224, new Piece("TGKK"));
	piecesNormal.put(225, new Piece("POKK"));
	piecesNormal.put(226, new Piece("SOKG"));
	piecesNormal.put(227, new Piece("LLKG"));
	piecesNormal.put(228, new Piece("SHKD"));
	piecesNormal.put(229, new Piece("GVKT"));
	piecesNormal.put(230, new Piece("PHKT"));
	piecesNormal.put(231, new Piece("LTKH"));
	piecesNormal.put(232, new Piece("LUKH"));
	piecesNormal.put(233, new Piece("STSS"));
	piecesNormal.put(234, new Piece("PDSG"));
	piecesNormal.put(235, new Piece("GDSD"));
	piecesNormal.put(236, new Piece("GTSD"));
	piecesNormal.put(237, new Piece("LOSD"));
	piecesNormal.put(238, new Piece("DPSL"));
	piecesNormal.put(239, new Piece("OVST"));
	piecesNormal.put(240, new Piece("UOST"));
	piecesNormal.put(241, new Piece("GUSH"));
	piecesNormal.put(242, new Piece("DUSH"));
	piecesNormal.put(243, new Piece("OLGO"));
	piecesNormal.put(244, new Piece("THGO"));
	piecesNormal.put(245, new Piece("VTGD"));
	piecesNormal.put(246, new Piece("PVGU"));
	piecesNormal.put(247, new Piece("UVOO"));
	piecesNormal.put(248, new Piece("LDOD"));
	piecesNormal.put(249, new Piece("DUOL"));
	piecesNormal.put(250, new Piece("PUOT"));
	piecesNormal.put(251, new Piece("VHDD"));
	piecesNormal.put(252, new Piece("HLDL"));
	piecesNormal.put(253, new Piece("PTLH"));
	piecesNormal.put(254, new Piece("UPTP"));
	piecesNormal.put(255, new Piece("PVTV"));
	piecesNormal.put(256, new Piece("UVHV"));
	
	// Place initial piece.
	placePiece(139, 16 * (9 - 1) + (8 - 1), 2, 0);
    }

    /**
     * Define default move pattern.
     * This is currently not so easy to modify since the different sets
     * depend on how deep the recursion is.
     */
    public void defaultMovePattern() {
	moveList = new int[255];
	defaultAddMove('H', 8);
	defaultAddMove('H', 9);
	defaultAddMove('I', 9);
	defaultAddMove('J', 9);
	defaultAddMove('J', 8);
	defaultAddMove('J', 7);
	defaultAddMove('I', 7);
	defaultAddMove('H', 7);
	defaultAddMove('G', 7);
	defaultAddMove('G', 8);
	defaultAddMove('G', 9);
	defaultAddMove('G', 10);
	defaultAddMove('H', 10);
	defaultAddMove('I', 10);
	defaultAddMove('J', 10);
	defaultAddMove('K', 10);
	defaultAddMove('K', 9);
	defaultAddMove('K', 8);
	defaultAddMove('K', 7);
	defaultAddMove('K', 6);
	defaultAddMove('J', 6);
	defaultAddMove('I', 6);
	defaultAddMove('H', 6);
	defaultAddMove('G', 6);
	defaultAddMove('F', 6);
	defaultAddMove('F', 7);
	defaultAddMove('F', 8);
	defaultAddMove('F', 9);
	defaultAddMove('F', 10);
	defaultAddMove('F', 11);
	defaultAddMove('G', 11);
	defaultAddMove('H', 11);
	defaultAddMove('I', 11);
	defaultAddMove('J', 11);
	defaultAddMove('K', 11);
	defaultAddMove('L', 11);
	defaultAddMove('L', 10);
	defaultAddMove('L', 9);
	defaultAddMove('L', 8);
	defaultAddMove('L', 7);
	defaultAddMove('L', 6);
	defaultAddMove('L', 5);
	defaultAddMove('K', 5);
	defaultAddMove('J', 5);
	defaultAddMove('I', 5);
	defaultAddMove('H', 5);
	defaultAddMove('G', 5);
	defaultAddMove('F', 5);
	defaultAddMove('E', 5);
	defaultAddMove('E', 6);
	defaultAddMove('E', 7);
	defaultAddMove('E', 8);
	defaultAddMove('E', 9);
	defaultAddMove('E', 10);
	defaultAddMove('E', 11);
	defaultAddMove('E', 12);
	defaultAddMove('F', 12);
	defaultAddMove('G', 12);
	defaultAddMove('H', 12);
	defaultAddMove('I', 12);
	defaultAddMove('J', 12);
	defaultAddMove('K', 12);
	defaultAddMove('L', 12);
	defaultAddMove('M', 12);
	defaultAddMove('M', 11);
	defaultAddMove('M', 10);
	defaultAddMove('M', 9);
	defaultAddMove('M', 8);
	defaultAddMove('M', 7);
	defaultAddMove('M', 6);
	defaultAddMove('M', 5);
	defaultAddMove('M', 4);
	defaultAddMove('L', 4);
	defaultAddMove('K', 4);
	defaultAddMove('J', 4);
	defaultAddMove('I', 4);
	defaultAddMove('H', 4);
	defaultAddMove('G', 4);
	defaultAddMove('F', 4);
	defaultAddMove('E', 4);
	defaultAddMove('D', 4);
	defaultAddMove('D', 5);
	defaultAddMove('D', 6);
	defaultAddMove('D', 7);
	defaultAddMove('D', 8);
	defaultAddMove('D', 9);
	defaultAddMove('D', 10);
	defaultAddMove('D', 11);
	defaultAddMove('D', 12);
	defaultAddMove('D', 13);
	defaultAddMove('E', 13);
	defaultAddMove('F', 13);
	defaultAddMove('G', 13);
	defaultAddMove('H', 13);
	defaultAddMove('I', 13);
	defaultAddMove('J', 13);
	defaultAddMove('K', 13);
	defaultAddMove('L', 13);
	defaultAddMove('M', 13);
	defaultAddMove('N', 13);
	defaultAddMove('N', 12);
	defaultAddMove('N', 11);
	defaultAddMove('N', 10);
	defaultAddMove('N', 9);
	defaultAddMove('N', 8);
	defaultAddMove('N', 7);
	defaultAddMove('N', 6);
	defaultAddMove('N', 5);
	defaultAddMove('N', 4);
	defaultAddMove('N', 3);
	defaultAddMove('M', 3);
	defaultAddMove('L', 3);
	defaultAddMove('K', 3);
	defaultAddMove('J', 3);
	defaultAddMove('I', 3);
	defaultAddMove('H', 3);
	defaultAddMove('G', 3);
	defaultAddMove('F', 3);
	defaultAddMove('E', 3);
	defaultAddMove('D', 3);
	defaultAddMove('C', 3);
	defaultAddMove('C', 4);
	defaultAddMove('C', 5);
	defaultAddMove('C', 6);
	defaultAddMove('C', 7);
	defaultAddMove('C', 8);
	defaultAddMove('C', 9);
	defaultAddMove('C', 10);
	defaultAddMove('C', 11);
	defaultAddMove('C', 12);
	defaultAddMove('C', 13);
	defaultAddMove('C', 14);
	defaultAddMove('D', 14);
	defaultAddMove('E', 14);
	defaultAddMove('F', 14);
	defaultAddMove('G', 14);
	defaultAddMove('H', 14);
	defaultAddMove('I', 14);
	defaultAddMove('J', 14);
	defaultAddMove('K', 14);
	defaultAddMove('L', 14);
	defaultAddMove('M', 14);
	defaultAddMove('N', 14);
	defaultAddMove('O', 14);
	defaultAddMove('O', 13);
	defaultAddMove('O', 12);
	defaultAddMove('O', 11);
	defaultAddMove('O', 10);
	defaultAddMove('O', 9);
	defaultAddMove('O', 8);
	defaultAddMove('O', 7);
	defaultAddMove('O', 6);
	defaultAddMove('O', 5);
	defaultAddMove('O', 4);
	defaultAddMove('O', 3);
	defaultAddMove('O', 2);
	defaultAddMove('N', 2);
	defaultAddMove('M', 2);
	defaultAddMove('L', 2);
	defaultAddMove('K', 2);
	defaultAddMove('J', 2);
	defaultAddMove('I', 2);
	defaultAddMove('H', 2);
	defaultAddMove('G', 2);
	defaultAddMove('F', 2);
	defaultAddMove('E', 2);
	defaultAddMove('D', 2);
	defaultAddMove('C', 2);
	defaultAddMove('B', 2);
	defaultAddMove('B', 3);
	defaultAddMove('B', 4);
	defaultAddMove('B', 5);
	defaultAddMove('B', 6);
	defaultAddMove('B', 7);
	defaultAddMove('B', 8);
	defaultAddMove('B', 9);
	defaultAddMove('B', 10);
	defaultAddMove('B', 11);
	defaultAddMove('B', 12);
	defaultAddMove('B', 13);
	defaultAddMove('B', 14);
	defaultAddMove('B', 15);
	defaultAddMove('C', 15);
	defaultAddMove('D', 15);
	defaultAddMove('E', 15);
	defaultAddMove('F', 15);
	defaultAddMove('G', 15);
	defaultAddMove('H', 15);
	defaultAddMove('I', 15);
	defaultAddMove('J', 15);
	defaultAddMove('K', 15);
	defaultAddMove('L', 15);
	defaultAddMove('M', 15);
	defaultAddMove('N', 15);
	defaultAddMove('O', 15);
	defaultAddMove('P', 15);
	defaultAddMove('P', 14);
	defaultAddMove('P', 13);
	defaultAddMove('P', 12);
	defaultAddMove('P', 11);
	defaultAddMove('P', 10);
	defaultAddMove('P', 9);
	defaultAddMove('P', 8);
	defaultAddMove('P', 7);
	defaultAddMove('P', 6);
	defaultAddMove('P', 5);
	defaultAddMove('P', 4);
	defaultAddMove('P', 3);
	defaultAddMove('P', 2);
	defaultAddMove('P', 1);
	defaultAddMove('O', 1);
	defaultAddMove('N', 1);
	defaultAddMove('M', 1);
	defaultAddMove('L', 1);
	defaultAddMove('K', 1);
	defaultAddMove('J', 1);
	defaultAddMove('I', 1);
	defaultAddMove('H', 1);
	defaultAddMove('G', 1);
	defaultAddMove('F', 1);
	defaultAddMove('E', 1);
	defaultAddMove('D', 1);
	defaultAddMove('C', 1);
	defaultAddMove('B', 1);
	defaultAddMove('A', 1);
	defaultAddMove('A', 2);
	defaultAddMove('A', 3);
	defaultAddMove('A', 4);
	defaultAddMove('A', 5);
	defaultAddMove('A', 6);
	defaultAddMove('A', 7);
	defaultAddMove('A', 8);
	defaultAddMove('A', 9);
	defaultAddMove('A', 10);
	defaultAddMove('A', 11);
	defaultAddMove('A', 12);
	defaultAddMove('A', 13);
	defaultAddMove('A', 14);
	defaultAddMove('A', 15);
	defaultAddMove('A', 16);
	defaultAddMove('B', 16);
	defaultAddMove('C', 16);
	defaultAddMove('D', 16);
	defaultAddMove('E', 16);
	defaultAddMove('F', 16);
	defaultAddMove('G', 16);
	defaultAddMove('H', 16);
	defaultAddMove('I', 16);
	defaultAddMove('J', 16);
	defaultAddMove('K', 16);
	defaultAddMove('L', 16);
	defaultAddMove('M', 16);
	defaultAddMove('N', 16);
	defaultAddMove('O', 16);
	defaultAddMove('P', 16);
    }

    /**
     * Translate board location in "human form" to array index.
     * 
     * @param row Row [A - P].
     * @param col Column [1 - 16]
     */
    public void defaultAddMove(char row, int col) {
	switch (row) {
	    case 'A':
		moveList[lastAddedMove] = col - 1;
		break;
	    case 'B':
		moveList[lastAddedMove] = 16 + col - 1;
		break;
	    case 'C':
		moveList[lastAddedMove] = 2 * 16 + col - 1;
		break;
	    case 'D':
		moveList[lastAddedMove] = 3 * 16 + col - 1;
		break;
	    case 'E':
		moveList[lastAddedMove] = 4 * 16 + col - 1;
		break;
	    case 'F':
		moveList[lastAddedMove] = 5 * 16 + col - 1;
		break;
	    case 'G':
		moveList[lastAddedMove] = 6 * 16 + col - 1;
		break;
	    case 'H':
		moveList[lastAddedMove] = 7 * 16 + col - 1;
		break;
	    case 'I':
		moveList[lastAddedMove] = 8 * 16 + col - 1;
		break;
	    case 'J':
		moveList[lastAddedMove] = 9 * 16 + col - 1;
		break;
	    case 'K':
		moveList[lastAddedMove] = 10 * 16 + col - 1;
		break;
	    case 'L':
		moveList[lastAddedMove] = 11 * 16 + col - 1;
		break;
	    case 'M':
		moveList[lastAddedMove] = 12 * 16 + col - 1;
		break;
	    case 'N':
		moveList[lastAddedMove] = 13 * 16 + col - 1;
		break;
	    case 'O':
		moveList[lastAddedMove] = 14 * 16 + col - 1;
		break;
	    case 'P':
		moveList[lastAddedMove] = 15 * 16 + col - 1;
		break;
	}

	lastAddedMove++;
    }

    /**
     * Start solving!
     * @param args Does nothing :)
     */
    public static void main(String[] args) {
	Eternity2 puzzle = new Eternity2();
	puzzle.loadPieces();
	puzzle.defaultMovePattern();
	System.out.println(puzzle.nextMove(0) ? "Puzzle solved!" : "Puzzle failed, check data.");
    }
}
