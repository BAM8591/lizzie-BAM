package featurecat.lizzie.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import featurecat.lizzie.Config;
import featurecat.lizzie.Lizzie;
import featurecat.lizzie.analysis.Leelaz;
import featurecat.lizzie.analysis.MoveData;
import java.io.IOException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class LzSgfPropertyTest {

  @Before
  public void setUp() throws IOException {
    if (Lizzie.config == null) {
      Lizzie.config = new Config();
    }
    if (Lizzie.board == null) {
      Lizzie.board = new Board();
    }
    if (Lizzie.leelaz == null) {
      Lizzie.leelaz = new Leelaz("");
    }
  }

  @Test
  public void testBasicLzPropertyParsing() throws IOException {
    String sgfWithLz =
        "(;GM[1]FF[4]SZ[19];B[pd]LZ[info move Q16 visits 1500 winrate 5420 pv Q16 D4 Q4])";

    boolean loaded = SGFParser.loadFromString(sgfWithLz);
    assertTrue("SGF should load successfully", loaded);

    // Move to the node with LZ property
    Lizzie.board.nextMove();

    BoardData data = Lizzie.board.getData();
    assertTrue("Should have LZ analysis data", data.hasLzAnalysis());
    assertNotNull("LZ candidates should not be null", data.lzCandidates);
    assertEquals("Should have one LZ candidate", 1, data.lzCandidates.size());

    MoveData lzMove = data.lzData;
    assertNotNull("LZ data should not be null", lzMove);
    assertEquals("Move coordinate should be Q16", "Q16", lzMove.coordinate);
    assertEquals("Visits should be 1500", 1500, lzMove.playouts);
    assertEquals("Winrate should be 54.20", 54.20, lzMove.winrate, 0.01);
    assertNotNull("PV should not be null", lzMove.variation);
    assertEquals("PV should start with Q16", "Q16", lzMove.variation.get(0));
  }

  @Test
  public void testMultipleLzCandidates() throws IOException {
    String sgfWithMultipleLz =
        "(;GM[1]FF[4]SZ[19];B[pd]LZ[info move Q16 visits 1500 winrate 5420 pv Q16 D4\n"
            + "info move D4 visits 1200 winrate 5380 pv D4 Q16 R6])";

    boolean loaded = SGFParser.loadFromString(sgfWithMultipleLz);
    assertTrue("SGF should load successfully", loaded);

    // Move to the node with LZ property
    Lizzie.board.nextMove();

    BoardData data = Lizzie.board.getData();
    assertTrue("Should have LZ analysis data", data.hasLzAnalysis());
    assertNotNull("LZ candidates should not be null", data.lzCandidates);
    assertEquals("Should have two LZ candidates", 2, data.lzCandidates.size());

    MoveData firstMove = data.lzCandidates.get(0);
    assertEquals("First move should be Q16", "Q16", firstMove.coordinate);
    assertEquals("First move visits should be 1500", 1500, firstMove.playouts);

    MoveData secondMove = data.lzCandidates.get(1);
    assertEquals("Second move should be D4", "D4", secondMove.coordinate);
    assertEquals("Second move visits should be 1200", 1200, secondMove.playouts);
  }

  @Test
  public void testKataGoFormat() throws IOException {
    String sgfWithKataGo =
        "(;GM[1]FF[4]SZ[19];B[pd]LZ[info move Q16 visits 100 winrate 0.542 scoreMean 2.5 pv Q16 D4])";

    boolean loaded = SGFParser.loadFromString(sgfWithKataGo);
    assertTrue("SGF should load successfully", loaded);

    // Move to the node with LZ property
    Lizzie.board.nextMove();

    BoardData data = Lizzie.board.getData();
    assertTrue("Should have LZ analysis data", data.hasLzAnalysis());

    MoveData lzMove = data.lzData;
    assertNotNull("LZ data should not be null", lzMove);
    assertEquals("Move coordinate should be Q16", "Q16", lzMove.coordinate);
    assertEquals("Visits should be 100", 100, lzMove.playouts);
    assertEquals("Winrate should be 54.2", 54.2, lzMove.winrate, 0.01);
    assertEquals("Score mean should be 2.5", 2.5, lzMove.scoreMean, 0.01);
  }

  @Test
  public void testEmptyLzProperty() throws IOException {
    String sgfWithEmptyLz = "(;GM[1]FF[4]SZ[19];B[pd]LZ[])";

    boolean loaded = SGFParser.loadFromString(sgfWithEmptyLz);
    assertTrue("SGF should load successfully", loaded);

    // Move to the node with LZ property
    Lizzie.board.nextMove();

    BoardData data = Lizzie.board.getData();
    assertNull("LZ data should be null for empty property", data.lzData);
    assertNull("LZ candidates should be null for empty property", data.lzCandidates);
  }

  @Test
  public void testNoLzProperty() throws IOException {
    String sgfWithoutLz = "(;GM[1]FF[4]SZ[19];B[pd])";

    boolean loaded = SGFParser.loadFromString(sgfWithoutLz);
    assertTrue("SGF should load successfully", loaded);

    // Move to the node without LZ property
    Lizzie.board.nextMove();

    BoardData data = Lizzie.board.getData();
    assertNull("LZ data should be null when no property", data.lzData);
    assertNull("LZ candidates should be null when no property", data.lzCandidates);
  }

  @Test
  public void testEffectiveAnalysis() throws IOException {
    String sgfWithLz =
        "(;GM[1]FF[4]SZ[19];B[pd]LZ[info move Q16 visits 1500 winrate 5420 pv Q16 D4])";

    boolean loaded = SGFParser.loadFromString(sgfWithLz);
    assertTrue("SGF should load successfully", loaded);

    // Move to the node with LZ property
    Lizzie.board.nextMove();

    BoardData data = Lizzie.board.getData();
    List<MoveData> effectiveAnalysis = data.getEffectiveAnalysis();

    assertNotNull("Effective analysis should not be null", effectiveAnalysis);
    assertEquals("Should return LZ candidates", data.lzCandidates, effectiveAnalysis);
    assertEquals("Should have one move", 1, effectiveAnalysis.size());
    assertEquals("Should be Q16", "Q16", effectiveAnalysis.get(0).coordinate);
  }

  @Test
  public void testEffectiveWinrateAndPlayouts() throws IOException {
    String sgfWithLz =
        "(;GM[1]FF[4]SZ[19];B[pd]LZ[info move Q16 visits 1500 winrate 5420 pv Q16 D4])";

    boolean loaded = SGFParser.loadFromString(sgfWithLz);
    assertTrue("SGF should load successfully", loaded);

    // Move to the node with LZ property
    Lizzie.board.nextMove();

    BoardData data = Lizzie.board.getData();

    // Since no live analysis is present (playouts == 0), should use LZ data
    assertEquals("Should use LZ winrate", 54.20, data.getWinrate(), 0.01);
    assertEquals("Should use LZ playouts", 1500, data.getPlayouts());
  }

  @Test
  public void testEffectiveScoreMean() throws IOException {
    String sgfWithKataGo =
        "(;GM[1]FF[4]SZ[19];B[pd]LZ[info move Q16 visits 100 winrate 5420 scoreMean 2.5 pv Q16 D4])";

    boolean loaded = SGFParser.loadFromString(sgfWithKataGo);
    assertTrue("SGF should load successfully", loaded);

    // Move to the node with LZ property
    Lizzie.board.nextMove();

    BoardData data = Lizzie.board.getData();

    // Should use LZ score mean when available
    assertEquals("Should use LZ score mean", 2.5, data.getScoreMean(), 0.01);
  }
}
