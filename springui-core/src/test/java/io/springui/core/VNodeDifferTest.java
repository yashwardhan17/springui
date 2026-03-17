package io.springui.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class VNodeDifferTest {

    private VNodeDiffer differ;

    @BeforeEach
    void setUp() {
        differ = new VNodeDiffer();
    }

    // ===========================
    // No Change
    // ===========================

    @Test
    void shouldProduceNoPatchesWhenNodesAreIdentical() {
        VNode oldNode = VNode.element("div").attr("class", "box");
        VNode newNode = VNode.element("div").attr("class", "box");
        List<VNodeDiffer.Patch> patches = differ.diff(oldNode, newNode);
        assertTrue(patches.isEmpty());
    }

    // ===========================
    // REPLACE
    // ===========================

    @Test
    void shouldProduceReplacePatchWhenTagChanges() {
        VNode oldNode = VNode.element("div");
        VNode newNode = VNode.element("span");
        List<VNodeDiffer.Patch> patches = differ.diff(oldNode, newNode);
        assertEquals(1, patches.size());
        assertEquals(VNodeDiffer.PatchType.REPLACE, patches.get(0).getType());
    }

    @Test
    void shouldProduceReplacePatchWhenElementBecomesTextNode() {
        VNode oldNode = VNode.element("div");
        VNode newNode = VNode.text("hello");
        List<VNodeDiffer.Patch> patches = differ.diff(oldNode, newNode);
        assertEquals(1, patches.size());
        assertEquals(VNodeDiffer.PatchType.REPLACE, patches.get(0).getType());
    }

    // ===========================
    // UPDATE_ATTRS
    // ===========================

    @Test
    void shouldProduceUpdateAttrsPatchWhenAttrChanges() {
        VNode oldNode = VNode.element("div").attr("class", "box");
        VNode newNode = VNode.element("div").attr("class", "container");
        List<VNodeDiffer.Patch> patches = differ.diff(oldNode, newNode);
        assertEquals(1, patches.size());
        assertEquals(VNodeDiffer.PatchType.UPDATE_ATTRS, patches.get(0).getType());
    }

    @Test
    void shouldProduceUpdateAttrsPatchWhenAttrAdded() {
        VNode oldNode = VNode.element("div");
        VNode newNode = VNode.element("div").attr("id", "main");
        List<VNodeDiffer.Patch> patches = differ.diff(oldNode, newNode);
        assertEquals(1, patches.size());
        assertEquals(VNodeDiffer.PatchType.UPDATE_ATTRS, patches.get(0).getType());
    }

    @Test
    void shouldProduceUpdateAttrsPatchWhenAttrRemoved() {
        VNode oldNode = VNode.element("div").attr("id", "main");
        VNode newNode = VNode.element("div");
        List<VNodeDiffer.Patch> patches = differ.diff(oldNode, newNode);
        assertEquals(1, patches.size());
        assertEquals(VNodeDiffer.PatchType.UPDATE_ATTRS, patches.get(0).getType());
    }

    // ===========================
    // TEXT_CHANGE
    // ===========================

    @Test
    void shouldProduceTextChangePatchWhenTextChanges() {
        VNode oldNode = VNode.text("hello");
        VNode newNode = VNode.text("world");
        List<VNodeDiffer.Patch> patches = differ.diff(oldNode, newNode);
        assertEquals(1, patches.size());
        assertEquals(VNodeDiffer.PatchType.TEXT_CHANGE, patches.get(0).getType());
    }

    @Test
    void shouldProduceNoPatchWhenTextIsIdentical() {
        VNode oldNode = VNode.text("hello");
        VNode newNode = VNode.text("hello");
        List<VNodeDiffer.Patch> patches = differ.diff(oldNode, newNode);
        assertTrue(patches.isEmpty());
    }

    // ===========================
    // ADD_CHILD / REMOVE_CHILD
    // ===========================

    @Test
    void shouldProduceAddChildPatchWhenChildAdded() {
        VNode oldNode = VNode.element("ul");
        VNode newNode = VNode.element("ul").child(VNode.element("li"));
        List<VNodeDiffer.Patch> patches = differ.diff(oldNode, newNode);
        assertEquals(1, patches.size());
        assertEquals(VNodeDiffer.PatchType.ADD_CHILD, patches.get(0).getType());
    }

    @Test
    void shouldProduceRemoveChildPatchWhenChildRemoved() {
        VNode oldNode = VNode.element("ul").child(VNode.element("li"));
        VNode newNode = VNode.element("ul");
        List<VNodeDiffer.Patch> patches = differ.diff(oldNode, newNode);
        assertEquals(1, patches.size());
        assertEquals(VNodeDiffer.PatchType.REMOVE_CHILD, patches.get(0).getType());
    }

    // ===========================
    // Nested / Complex
    // ===========================

    @Test
    void shouldDiffNestedChildrenCorrectly() {
        VNode oldNode = VNode.element("div")
                .child(VNode.element("h1").child(VNode.text("Title")));
        VNode newNode = VNode.element("div")
                .child(VNode.element("h1").child(VNode.text("Updated Title")));
        List<VNodeDiffer.Patch> patches = differ.diff(oldNode, newNode);
        assertEquals(1, patches.size());
        assertEquals(VNodeDiffer.PatchType.TEXT_CHANGE, patches.get(0).getType());
    }
}