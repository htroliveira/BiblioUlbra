package com.hgianastasio.biblioulbrav2.core.historybooks.repository

import com.hgianastasio.biblioulbrav2.core.base.boundaries.OnResultListener
import com.hgianastasio.biblioulbrav2.core.historybooks.HistoryBook
import java.io.IOException

/**
 * Created by heitorgianastasio on 4/24/17.
 */
interface HistoryBooksRepository {
    @Throws(IOException::class)
    fun get(cgu: String?, onResultListener: OnResultListener<List<HistoryBook>>)

    @Throws(IOException::class)
    fun reload(cgu: String?, onResultListener: OnResultListener<List<HistoryBook>>)
}