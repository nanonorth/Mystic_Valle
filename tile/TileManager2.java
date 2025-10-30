package tile;

import java.util.List;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import entity.Player;

public class TileManager2 {
	
	// Encapsulation

    private final ArrayList<Tile2[][]> baseLayers = new ArrayList<>();
    private final ArrayList<Tile2[][]> topLayers  = new ArrayList<>();
    private int mapWidth, mapHeight, tileWidth, tileHeight;
    private boolean[][] collisionMap;

    private static class Tileset {
        int firstgid;
        BufferedImage image;
        int tilesPerRow;
        String tsxPath;
        Map<Integer, Boolean> collisionTiles = new HashMap<>();
        Map<Integer, String>  tileTypes      = new HashMap<>();
    }
    private final ArrayList<Tileset> tilesets = new ArrayList<>();

    private static final long FLIPPED_HORIZONTALLY_FLAG = 0x80000000L;
    private static final long FLIPPED_VERTICALLY_FLAG   = 0x40000000L;
    private static final long FLIPPED_DIAGONALLY_FLAG   = 0x20000000L;
    private static final long GID_MASK = ~(FLIPPED_HORIZONTALLY_FLAG | FLIPPED_VERTICALLY_FLAG | FLIPPED_DIAGONALLY_FLAG);

    //
    
    
    public TileManager2(String mapPath) {
        try {
            String jsonText = new String(Files.readAllBytes(Paths.get(mapPath)));
            JSONObject json = new JSONObject(jsonText);

            mapWidth  = json.getInt("width");
            mapHeight = json.getInt("height");
            tileWidth = json.getInt("tilewidth");
            tileHeight= json.getInt("tileheight");

            collisionMap = new boolean[mapHeight][mapWidth];

            loadTilesets(json, mapPath);
            loadLayers(json);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadTilesets(JSONObject json, String mapPath) throws Exception {
        JSONArray tilesetArray = json.getJSONArray("tilesets");
        String baseDir = new File(mapPath).getParent();

        for (int i = 0; i < tilesetArray.length(); i++) {
            JSONObject ts = tilesetArray.getJSONObject(i);
            Tileset t = new Tileset();
            t.firstgid = ts.getInt("firstgid");

            String imageSource;
            if (ts.has("source")) {
                String tsxFile = ts.getString("source");
                t.tsxPath = baseDir + File.separator + tsxFile;

                String tsxText = new String(Files.readAllBytes(Paths.get(t.tsxPath)));
                JSONObject tilesetJson = XML.toJSONObject(tsxText).getJSONObject("tileset");
                imageSource = tilesetJson.getJSONObject("image").getString("source");

                if (tilesetJson.has("tile")) {
                    Object tileObj = tilesetJson.get("tile");
                    JSONArray tileArray = (tileObj instanceof JSONArray) ? (JSONArray) tileObj
                            : new JSONArray(Collections.singletonList(tileObj));
                    for (int ti = 0; ti < tileArray.length(); ti++) {
                        JSONObject tileJson = tileArray.getJSONObject(ti);
                        int id = tileJson.getInt("id");
                        if (tileJson.has("properties")) {
                            Object propObj = tileJson.getJSONObject("properties").get("property");
                            JSONArray props = (propObj instanceof JSONArray) ? (JSONArray) propObj
                                    : new JSONArray(Collections.singletonList(propObj));
                            for (int p = 0; p < props.length(); p++) {
                                JSONObject pObj = props.getJSONObject(p);
                                String name = pObj.getString("name");
                                if (name.equalsIgnoreCase("collision"))
                                    t.collisionTiles.put(id, pObj.getBoolean("value"));
                                if (name.equalsIgnoreCase("type"))
                                    t.tileTypes.put(id, pObj.getString("value"));
                            }
                        }
                    }
                }
            } else {
                imageSource = ts.getString("image");
            }

            File imgFile = new File(baseDir, imageSource);
            if (imgFile.exists()) {
                t.image = ImageIO.read(imgFile);
            } else {
                try (var stream = getClass().getResourceAsStream("/" + imageSource.replace("\\","/"))) {
                    if (stream == null) throw new RuntimeException("Tileset image not found: " + imageSource);
                    t.image = ImageIO.read(stream);
                }
            }

            t.tilesPerRow = t.image.getWidth() / tileWidth;
            tilesets.add(t);
        }
    }

    private Tileset pickTileset(int gid) {
        for (int i = tilesets.size()-1; i>=0; i--) if (gid >= tilesets.get(i).firstgid) return tilesets.get(i);
        return null;
    }

    private BufferedImage applyFlip(BufferedImage src, boolean flipH, boolean flipV, boolean flipD) {
        if (!flipH && !flipV && !flipD) return src;
        BufferedImage out = new BufferedImage(tileWidth, tileHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        AffineTransform at = new AffineTransform();
        if (flipD) { at.translate(tileHeight, 0); at.rotate(Math.toRadians(-90)); }
        if (flipH) { at.translate(tileWidth, 0); at.scale(-1, 1); }
        if (flipV) { at.translate(0, tileHeight); at.scale(1, -1); }
        g2.drawImage(src, at, null); g2.dispose();
        return out;
    }

    private void loadLayers(JSONObject json) throws Exception {
        JSONArray layers = json.getJSONArray("layers");
        for (int li = 0; li < layers.length(); li++) {
            JSONObject layer = layers.getJSONObject(li);
            if (!"tilelayer".equals(layer.getString("type"))) continue;

            String name = layer.optString("name","").toLowerCase();
            boolean isTopLayer  = name.contains("top");
            boolean isBaseLayer = !isTopLayer;
            
            boolean isDungeonWall = name.equals("wall") ||
                    name.equals("wall1") ||
                    name.equals("wall2") ||
                    name.equals("wall3") ||
                    name.equals("wall4") ||
                    name.equals("wall5");

            JSONArray data = layer.getJSONArray("data");
            Tile2[][] buf = new Tile2[mapHeight][mapWidth];

            for (int y = 0; y < mapHeight; y++) {
                for (int x = 0; x < mapWidth; x++) {

                    long raw = data.getLong(y * mapWidth + x);
                    if (raw == 0) continue;

                    boolean flipH = (raw & FLIPPED_HORIZONTALLY_FLAG) != 0;
                    boolean flipV = (raw & FLIPPED_VERTICALLY_FLAG) != 0;
                    boolean flipD = (raw & FLIPPED_DIAGONALLY_FLAG) != 0;
                    int gid = (int)(raw & GID_MASK);

                    Tileset ts = pickTileset(gid);
                    int localId = gid - ts.firstgid;
                    int sx = (localId % ts.tilesPerRow) * tileWidth;
                    int sy = (localId / ts.tilesPerRow) * tileHeight;

                    BufferedImage tileImg = applyFlip(
                        ts.image.getSubimage(sx, sy, tileWidth, tileHeight),
                        flipH, flipV, flipD);

                    boolean solid = ts.collisionTiles.getOrDefault(localId, false);
                    
                    if (isDungeonWall) solid = true;
                    
                    buf[y][x] = new Tile2(tileImg, solid);
                    if (solid) collisionMap[y][x] = true;

                    // base worldBottomY
                    buf[y][x].worldBottomY = (y + 1) * tileHeight;

                    if (isTopLayer) {
                        String type = ts.tileTypes.getOrDefault(localId, "");

                        if (type.equals("roof")) {
                        	buf[y][x].worldBottomY += tileHeight * 9;
                        } else if (type.equals("treetop")) {
                            buf[y][x].worldBottomY += tileHeight * 2.3;
                        }
                    }
                }
            }

            if (isBaseLayer) baseLayers.add(buf);
            if (isTopLayer)  topLayers.add(buf);
        }
    }
    
    // Abstraction

    public void render(Graphics2D g, int camX, int camY, int sw, int sh, double zoom, Player player) {

        // Base
        for (Tile2[][] layer : baseLayers) {
            for (int y=0; y<mapHeight; y++)
                for (int x=0; x<mapWidth; x++)
                    if (layer[y][x]!=null) drawTile(g, layer[y][x], x, y, camX, camY, sw, sh, zoom);
        }

        // Buckets + player depth
        List<RenderBucket> bucket = new ArrayList<>();

        for (Tile2[][] layer : topLayers) {
            for (int y=0; y<mapHeight; y++) {
                for (int x=0; x<mapWidth; x++) {
                    Tile2 t = layer[y][x]; if (t==null) continue;
                    final int tx=x, ty=y; final Tile2 tt=t;
                    bucket.add(new RenderBucket(){{
                        sortY = tt.worldBottomY;
                        drawCall = () -> drawTile(g, tt, tx, ty, camX, camY, sw, sh, zoom);
                    }});
                }
            }
        }

        bucket.add(new RenderBucket(){{
            sortY = player.depthY;
            drawCall = () -> player.draw(g, camX, camY, zoom);
        }});

        bucket.sort(Comparator.comparingInt(a -> a.sortY));
        bucket.forEach(RenderBucket::run);
    }

    
    // Polymorphism
    
    private void drawTile(Graphics2D g, Tile2 t, int x, int y, int camX, int camY, int sw, int sh, double zoom) {
        int dx = (int)((x * tileWidth  - camX) * zoom);
        int dy = (int)((y * tileHeight - camY) * zoom);
        int dw = (int)(tileWidth  * zoom);
        int dh = (int)(tileHeight * zoom);
        g.drawImage(t.getImage(), dx, dy, dw, dh, null);
    }

    
    // Polymorphism
    
    
    public boolean isSolidAtPixel(int px, int py) {
        int tx = tileWidth  == 0 ? 0 : px / tileWidth;
        int ty = tileHeight == 0 ? 0 : py / tileHeight;
        if (tx < 0 || ty < 0 || tx >= mapWidth || ty >= mapHeight) return true;
        return collisionMap[ty][tx];
    }

    public boolean isTopTileAtPixel(int px, int py) {
        int tx = tileWidth  == 0 ? 0 : px / tileWidth;
        int ty = tileHeight == 0 ? 0 : py / tileHeight;
        if (tx < 0 || ty < 0 || tx >= mapWidth || ty >= mapHeight) return false;
        for (Tile2[][] layer : topLayers) if (layer[ty][tx] != null) return true;
        return false;
    }

    // Debug overlays
    public void drawCollisionDebug(Graphics2D g, int camX, int camY, int sw, int sh, double zoom) {
        g.setColor(new Color(255, 0, 0, 120));
        for (int y=0; y<mapHeight; y++) for (int x=0; x<mapWidth; x++) {
            if (!collisionMap[y][x]) continue;
            int dx = (int)((x * tileWidth  - camX) * zoom);
            int dy = (int)((y * tileHeight - camY) * zoom);
            int dw = (int)(tileWidth  * zoom);
            int dh = (int)(tileHeight * zoom);
            if (dx + dw < 0 || dy + dh < 0 || dx > sw || dy > sh) continue;
            g.fillRect(dx, dy, dw, dh);
        }
    }

    public void drawDebugWarp(Graphics2D g, int camX, int camY, double zoom, int warpX, int warpY) {

        int dx = (int)((warpX * tileWidth  - camX) * zoom);
        int dy = (int)((warpY * tileHeight - camY) * zoom);

        // ✅ ใหญ่ขึ้น ~2.2 เท่า (จาก 1x tile)
        int dw = (int)(tileWidth  * zoom * 2.2);
        int dh = (int)(tileHeight * zoom * 2.2);

        long t = System.currentTimeMillis();
        
        // เคลื่อนไหวช้าลง
        double pulse = (Math.sin(t * 0.002) + 1) / 2; // 0 → 1
        float alpha = (float)(0.45f + pulse * 0.25f);

        // Outer Ice Glow
        g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
        g.setColor(new Color(20, 40, 100, 180));
        g.fillOval(dx - 20, dy - 20, dw + 40, dh + 40);

        // Center Gradient : Dark Frost
        RadialGradientPaint paint =
            new RadialGradientPaint(
                new java.awt.geom.Point2D.Double(dx + dw/2, dy + dh/2),
                dw/2f,
                new float[]{0f, 0.6f, 1f},
                new Color[]{
                    new Color(210, 240, 255, 240),
                    new Color(10, 90, 180, 220),
                    new Color(0, 20, 60, 240)
                }
            );
        g.setPaint(paint);
        g.fillOval(dx, dy, dw, dh);

        // Broken Ice Particles
        g.setColor(new Color(200, 230, 255, 200));
        for (int i = 0; i < 20; i++) {
            double ang = Math.random() * Math.PI * 2;
            int px = dx + dw/2 + (int)(Math.cos(ang) * (pulse * dw*0.8));
            int py = dy + dh/2 + (int)(Math.sin(ang) * (pulse * dh*0.8));
            g.fillRect(px, py, 4, 4);
        }

        // Outline (พลังมากขึ้น)
        g.setColor(new Color(255, 255, 255, 200));
        g.drawOval(dx, dy, dw, dh);

        g.setComposite(java.awt.AlphaComposite.SrcOver);
    }

    // Getters
    public int getMapWidth(){ return mapWidth; }
    public int getMapHeight(){ return mapHeight; }
    public int getTileWidth(){ return tileWidth; }
    public int getTileHeight(){ return tileHeight; }

    private static class RenderBucket {
        int sortY; Runnable drawCall; void run(){ drawCall.run(); }
    }
}