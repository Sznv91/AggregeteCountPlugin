package com.tibbo.aggregate.common.datatable.encoding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.tibbo.aggregate.common.datatable.SimpleDataTable;
import com.tibbo.aggregate.common.datatable.TableFormat;

/**
 * @author Vladimir Plizga
 * @since 19.10.2022
 */
public class LocalFormatCacheTest
{
    private LocalFormatCache sut;
    private TableFormat tableFormat;

    @Before
    public void setUp() throws Exception
    {
        sut = new LocalFormatCache("test");

        tableFormat = new TableFormat();
        tableFormat.makeImmutable(new SimpleDataTable());
    }

    @Test
    public void singleFormatGetsAssociatedWithCache()
    {
        // given

        // when
        sut.addIfNotExists(tableFormat);

        // then
        assertEquals(1, sut.getSize());
        assertNotNull(tableFormat.getId());
        assertEquals(0, tableFormat.getId().intValue());
    }

    @Test
    public void secondAdditionOfTheSameFormatDoesntChangeCache()
    {
        // given

        // when
        sut.addIfNotExists(tableFormat);
        sut.addIfNotExists(tableFormat);

        // then
        assertEquals(1, sut.getSize());
        assertNotNull(tableFormat.getId());
        assertEquals(0, tableFormat.getId().intValue());
    }

    @Test(expected = IllegalStateException.class)
    public void addingCachedFormatToEmptiedCacheIsPreventedByException()
    {
        // given
        sut.addIfNotExists(tableFormat);
        sut.clear();

        // when
        sut.addIfNotExists(tableFormat);

        // then
        fail("The cache must not add formats that already contain its ID but aren't actually contained in it");
    }

    @Test
    public void notAddedFormatHasNoId()
    {
        // given

        // when
        Integer id = sut.obtainId(tableFormat);

        // then
        assertNull("Format must not receive ID until it was added to cache", tableFormat.getId());
        assertNull("Cache must not assign ID to a format until it was explicitly added", id);
    }

    @Test
    public void addingEqualFormatDoesntChangeCache()
    {
        // given
        TableFormat formatClone = tableFormat.clone();
        formatClone.makeImmutable(new SimpleDataTable());
        formatClone.setId(null);

        sut.addIfNotExists(tableFormat);

        // when
        sut.addIfNotExists(formatClone);

        // then
        assertEquals("Adding an equal format must not fill the cache", 1, sut.getSize());
        assertNull("Equal format must not receive the same ID as already cached one", formatClone.getId());
    }

    @Test
    public void addingExternallyCachedFormatDoesntChangeCache()
    {
        // given
        tableFormat.setFormatCacheIdentityHashCode(1111111111);
        int externalId = 333333333;
        tableFormat.setId(externalId);

        // when
        sut.addIfNotExists(tableFormat);

        // then
        assertEquals("Adding an externally cached format must not fill the cache", 0, sut.getSize());
        assertNotNull(tableFormat.getId());
        assertEquals("Externally cached format must not change its ID after (idle) adding to local cache", externalId,
                tableFormat.getId().intValue());
    }

}