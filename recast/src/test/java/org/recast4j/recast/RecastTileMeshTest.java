/*
recast4j Copyright (c) 2015-2019 Piotr Piastucki piotr@jtilia.org

This software is provided 'as-is', without any express or implied
warranty.  In no event will the authors be held liable for any damages
arising from the use of this software.
Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it
freely, subject to the following restrictions:
1. The origin of this software must not be misrepresented; you must not
 claim that you wrote the original software. If you use this software
 in a product, an acknowledgment in the product documentation would be
 appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be
 misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/
package org.recast4j.recast;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.recast4j.recast.RecastBuilder.RecastBuilderResult;
import org.recast4j.recast.RecastConstants.PartitionType;
import org.recast4j.recast.geom.InputGeomProvider;

public class RecastTileMeshTest {

    private final float m_cellSize = 0.3f;
    private final float m_cellHeight = 0.2f;
    private final float m_agentHeight = 2.0f;
    private final float m_agentRadius = 0.6f;
    private final float m_agentMaxClimb = 0.9f;
    private final float m_agentMaxSlope = 45.0f;
    private final int m_regionMinSize = 8;
    private final int m_regionMergeSize = 20;
    private final float m_regionMinArea = m_regionMinSize * m_regionMinSize * m_cellSize * m_cellSize;
    private final float m_regionMergeArea = m_regionMergeSize * m_regionMergeSize * m_cellSize * m_cellSize;
    private final float m_edgeMaxLen = 12.0f;
    private final float m_edgeMaxError = 1.3f;
    private final int m_vertsPerPoly = 6;
    private final float m_detailSampleDist = 6.0f;
    private final float m_detailSampleMaxError = 1.0f;
    private final PartitionType m_partitionType = PartitionType.WATERSHED;
    private final int m_tileSize = 32;

    @Test
    public void testDungeon() {
        testBuild("dungeon.obj");
    }

    public void testBuild(String filename) {
        ObjImporter importer = new ObjImporter();
        InputGeomProvider geom = importer.load(getClass().getResourceAsStream(filename));
        RecastBuilder builder = new RecastBuilder();
        RecastConfig cfg = new RecastConfig(true, m_tileSize, m_tileSize, RecastConfig.calcBorder(m_agentRadius, m_cellSize),
                m_partitionType, m_cellSize, m_cellHeight, m_agentMaxSlope, true, true, true, m_agentHeight, m_agentRadius,
                m_agentMaxClimb, m_regionMinArea, m_regionMergeArea, m_edgeMaxLen, m_edgeMaxError, m_vertsPerPoly, true,
                m_detailSampleDist, m_detailSampleMaxError, SampleAreaModifications.SAMPLE_AREAMOD_GROUND);
        RecastBuilderConfig bcfg = new RecastBuilderConfig(cfg, geom.getMeshBoundsMin(), geom.getMeshBoundsMax(), 7, 8);
        RecastBuilderResult rcResult = builder.build(geom, bcfg);
        assertThat(rcResult.getMesh().npolys).isEqualTo(1);
        assertThat(rcResult.getMesh().nverts).isEqualTo(5);
        bcfg = new RecastBuilderConfig(cfg, geom.getMeshBoundsMin(), geom.getMeshBoundsMax(), 6, 9);
        rcResult = builder.build(geom, bcfg);
        assertThat(rcResult.getMesh().npolys).isEqualTo(2);
        assertThat(rcResult.getMesh().nverts).isEqualTo(7);
        bcfg = new RecastBuilderConfig(cfg, geom.getMeshBoundsMin(), geom.getMeshBoundsMax(), 2, 9);
        rcResult = builder.build(geom, bcfg);
        assertThat(rcResult.getMesh().npolys).isEqualTo(2);
        assertThat(rcResult.getMesh().nverts).isEqualTo(9);
        bcfg = new RecastBuilderConfig(cfg, geom.getMeshBoundsMin(), geom.getMeshBoundsMax(), 4, 3);
        rcResult = builder.build(geom, bcfg);
        assertThat(rcResult.getMesh().npolys).isEqualTo(3);
        assertThat(rcResult.getMesh().nverts).isEqualTo(6);
        bcfg = new RecastBuilderConfig(cfg, geom.getMeshBoundsMin(), geom.getMeshBoundsMax(), 2, 8);
        rcResult = builder.build(geom, bcfg);
        assertThat(rcResult.getMesh().npolys).isEqualTo(5);
        assertThat(rcResult.getMesh().nverts).isEqualTo(17);
        bcfg = new RecastBuilderConfig(cfg, geom.getMeshBoundsMin(), geom.getMeshBoundsMax(), 0, 8);
        rcResult = builder.build(geom, bcfg);
        assertThat(rcResult.getMesh().npolys).isEqualTo(6);
        assertThat(rcResult.getMesh().nverts).isEqualTo(15);
    }

    @Test
    public void testDungeonPerformance() {
        testPerformance("dungeon.obj");
    }

    @Test
    public void testNavTestPerformance() {
        testPerformance("nav_test.obj");
    }

    public void testPerformance(String filename) {
        ObjImporter importer = new ObjImporter();
        InputGeomProvider geom = importer.load(getClass().getResourceAsStream(filename));
        RecastBuilder builder = new RecastBuilder();
        RecastConfig cfg = new RecastConfig(true, m_tileSize, m_tileSize, RecastConfig.calcBorder(m_agentRadius, m_cellSize),
                m_partitionType, m_cellSize, m_cellHeight, m_agentMaxSlope, true, true, true, m_agentHeight, m_agentRadius,
                m_agentMaxClimb, m_regionMinArea, m_regionMergeArea, m_edgeMaxLen, m_edgeMaxError, m_vertsPerPoly, true,
                m_detailSampleDist, m_detailSampleMaxError, SampleAreaModifications.SAMPLE_AREAMOD_GROUND);
        for (int i = 0; i < 4; i++) {
            build(geom, builder, cfg, 1, false);
            build(geom, builder, cfg, 4, false);
            build(geom, builder, cfg, 8, false);
            build(geom, builder, cfg, 16, false);
        }
        long t1 = System.nanoTime();
        for (int i = 0; i < 8; i++) {
            build(geom, builder, cfg, 1, false);
        }
        long t2 = System.nanoTime();
        for (int i = 0; i < 8; i++) {
            build(geom, builder, cfg, 4, false);
        }
        long t3 = System.nanoTime();
        for (int i = 0; i < 8; i++) {
            build(geom, builder, cfg, 8, false);
        }
        long t4 = System.nanoTime();
        for (int i = 0; i < 8; i++) {
            build(geom, builder, cfg, 16, false);
        }
        long t5 = System.nanoTime();
        System.out.println(" Time 1 : " + (t2 - t1) / 1000000 + " ms");
        System.out.println(" Time 4 : " + (t3 - t2) / 1000000 + " ms");
        System.out.println(" Time 8 : " + (t4 - t3) / 1000000 + " ms");
        System.out.println(" Time 16 : " + (t5 - t4) / 1000000 + " ms");
    }

    private void build(InputGeomProvider geom, RecastBuilder builder, RecastConfig cfg, int threads, boolean validate) {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<RecastBuilderResult> tiles = builder.buildTiles(geom, cfg, Optional.of(executor));
        if (validate) {
            RecastBuilderResult rcResult = getTile(tiles, 7, 8);
            assertThat(rcResult.getMesh().npolys).isEqualTo(1);
            assertThat(rcResult.getMesh().nverts).isEqualTo(5);
            rcResult = getTile(tiles, 6, 9);
            assertThat(rcResult.getMesh().npolys).isEqualTo(2);
            assertThat(rcResult.getMesh().nverts).isEqualTo(7);
            rcResult = getTile(tiles, 2, 9);
            assertThat(rcResult.getMesh().npolys).isEqualTo(2);
            assertThat(rcResult.getMesh().nverts).isEqualTo(9);
            rcResult = getTile(tiles, 4, 3);
            assertThat(rcResult.getMesh().npolys).isEqualTo(3);
            assertThat(rcResult.getMesh().nverts).isEqualTo(6);
            rcResult = getTile(tiles, 2, 8);
            assertThat(rcResult.getMesh().npolys).isEqualTo(5);
            assertThat(rcResult.getMesh().nverts).isEqualTo(17);
            rcResult = getTile(tiles, 0, 8);
            assertThat(rcResult.getMesh().npolys).isEqualTo(6);
            assertThat(rcResult.getMesh().nverts).isEqualTo(15);
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.HOURS);
        } catch (InterruptedException e) {
        }
    }

    private RecastBuilderResult getTile(List<RecastBuilderResult> tiles, int x, int z) {
        return tiles.stream().filter(tile -> tile.tileX == x && tile.tileZ == z).findFirst().orElse(null);
    }
}
