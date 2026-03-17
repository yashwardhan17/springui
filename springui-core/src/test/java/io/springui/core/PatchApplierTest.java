package io.springui.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PatchApplierTest {

    private PatchApplier patchApplier;
    private VNodeDiffer differ;

    @BeforeEach
    void setUp() {
        patchApplier = new PatchApplier();
        differ = new VNodeDiffer();
    }

    @Test
    void shouldApplyNoPatchesWhenNodesIdentical() {
        VNode oldNode = VNode.element("div").attr("class", "box");
        VNode newNode = VNode.element("div").attr("class", "box");
        List<VNodeDiffer.Patch> patches = differ.diff(oldNode, newNode);
        assertDoesNotThrow(() -> patchApplier.applyPatches(patches));
        assertTrue(patches.isEmpty());
    }

    @Test
    void shouldApplyReplacePatch() {
        VNode oldNode = VNode.element("div");
        VNode newNode = VNode.element("span");
        List<VNodeDiffer.Patch> patches = differ.diff(oldNode, newNode);
        assertDoesNotThrow(() -> patchApplier.applyPatches(patches));
        assertEquals(1, patches.size());
        assertEquals(VNodeDiffer.PatchType.REPLACE, patches.get(0).getType());
    }

    @Test
    void shouldApplyUpdateAttrsPatch() {
        VNode oldNode = VNode.element("div").attr("class", "old");
        VNode newNode = VNode.element("div").attr("class", "new");
        List<VNodeDiffer.Patch> patches = differ.diff(oldNode, newNode);
        assertDoesNotThrow(() -> patchApplier.applyPatches(patches));
        assertEquals(1, patches.size());
        assertEquals(VNodeDiffer.PatchType.UPDATE_ATTRS, patches.get(0).getType());
    }

    @Test
    void shouldApplyTextChangePatch() {
        VNode oldNode = VNode.text("hello");
        VNode newNode = VNode.text("world");
        List<VNodeDiffer.Patch> patches = differ.diff(oldNode, newNode);
        assertDoesNotThrow(() -> patchApplier.applyPatches(patches));
        assertEquals(1, patches.size());
        assertEquals(VNodeDiffer.PatchType.TEXT_CHANGE, patches.get(0).getType());
    }

    @Test
    void shouldApplyAddChildPatch() {
        VNode oldNode = VNode.element("ul");
        VNode newNode = VNode.element("ul").child(VNode.element("li"));
        List<VNodeDiffer.Patch> patches = differ.diff(oldNode, newNode);
        assertDoesNotThrow(() -> patchApplier.applyPatches(patches));
        assertEquals(1, patches.size());
        assertEquals(VNodeDiffer.PatchType.ADD_CHILD, patches.get(0).getType());
    }

    @Test
    void shouldApplyRemoveChildPatch() {
        VNode oldNode = VNode.element("ul").child(VNode.element("li"));
        VNode newNode = VNode.element("ul");
        List<VNodeDiffer.Patch> patches = differ.diff(oldNode, newNode);
        assertDoesNotThrow(() -> patchApplier.applyPatches(patches));
        assertEquals(1, patches.size());
        assertEquals(VNodeDiffer.PatchType.REMOVE_CHILD, patches.get(0).getType());
    }

    @Test
    void shouldApplyMultiplePatches() {
        VNode oldNode = VNode.element("div")
                .attr("class", "old")
                .child(VNode.text("hello"));
        VNode newNode = VNode.element("div")
                .attr("class", "new")
                .child(VNode.text("world"));
        List<VNodeDiffer.Patch> patches = differ.diff(oldNode, newNode);
        assertDoesNotThrow(() -> patchApplier.applyPatches(patches));
        assertEquals(2, patches.size());
    }
}