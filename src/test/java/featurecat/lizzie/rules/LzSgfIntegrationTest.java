package featurecat.lizzie.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import featurecat.lizzie.Config;
import featurecat.lizzie.Lizzie;
import featurecat.lizzie.analysis.Leelaz;
import featurecat.lizzie.analysis.MoveData;
import java.io.IOException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class LzSgfIntegrationTest {

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
  public void testCompleteGame() throws IOException {
    boolean loaded = SGFParser.load("/tmp/test_lz_analysis.sgf");
    assertTrue("SGF should load successfully", loaded);

    // Test first move (B[pd])
    Lizzie.board.nextMove();
    BoardData data1 = Lizzie.board.getData();
    assertTrue("First move should have LZ analysis", data1.hasLzAnalysis());
    assertEquals("First move should have 3 candidates", 3, data1.lzCandidates.size());
    assertEquals("Best move should be Q16", "Q16", data1.lzData.coordinate);
    assertEquals("Winrate should be from LZ data", 54.20, data1.getWinrate(), 0.01);
    assertEquals("Playouts should be from LZ data", 1500, data1.getPlayouts());
    assertEquals("Score mean should be from LZ data", 1.2, data1.getScoreMean(), 0.01);

    // Test second move (W[dp])
    Lizzie.board.nextMove();
    BoardData data2 = Lizzie.board.getData();
    assertTrue("Second move should have LZ analysis", data2.hasLzAnalysis());
    assertEquals("Second move should have 2 candidates", 2, data2.lzCandidates.size());
    assertEquals("Best move should be D4", "D4", data2.lzData.coordinate);
    assertEquals("Winrate should be from LZ data", 45.80, data2.getWinrate(), 0.01);

    // Test third move (B[pp])
    Lizzie.board.nextMove();
    BoardData data3 = Lizzie.board.getData();
    assertTrue("Third move should have LZ analysis", data3.hasLzAnalysis());
    assertEquals("Third move should have 2 candidates", 2, data3.lzCandidates.size());
    assertEquals("Best move should be Q4", "Q4", data3.lzData.coordinate);

    // Test effective analysis
    List<MoveData> effectiveAnalysis = data3.getEffectiveAnalysis();
    assertEquals(
        "Effective analysis should return LZ candidates", data3.lzCandidates, effectiveAnalysis);
  }
}
