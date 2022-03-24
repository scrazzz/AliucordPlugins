package com.scruzism.plugins

data class Subpods(val plaintext: String)

data class Result(val queryresult: QueryResult?)
{
    data class QueryResult(val inputstring: String?, val pods: List<Pods>?)
    {
        data class Pods(val subpods: List<Subpods>?) // main thing
    }
}