package com.github.goldin.plugins.gradle.crawler
import org.gcontracts.annotations.Ensures
import org.gcontracts.annotations.Requires


/**
 * Links reporting storage
 */
class LinksReport
{
    private final Set<String>               processedLinks = [] as Set
    private final Map<String, List<String>> brokenLinks    = [:].withDefault{[]}
    private volatile boolean                locked         = false


    void lock()
    {
        locked = true
    }


    @Ensures({ result != null })
    Set<String> processedLinks()
    {
        assert locked
        processedLinks.asImmutable()
    }


    @Ensures({ result != null })
    Set<String> brokenLinks()
    {
        assert locked
        brokenLinks.keySet().asImmutable()
    }


    @Requires({ link })
    @Ensures({ result })
    List<String> brokenLinkReferrers( String link )
    {
        assert locked
        brokenLinks[ link ]
    }


    @Ensures({ result >= 0 })
    int processedLinksNumber()
    {
        processedLinks.size()
    }


    @Ensures({ result >= 0 })
    int brokenLinksNumber()
    {
        brokenLinks.size()
    }


    @Requires({ link })
    boolean isProcessedLink( String link )
    {
        link in processedLinks
    }


    @Requires({ links })
    @Ensures({ result != null })
    List<String> addLinksToProcess ( Collection<String> links )
    {
        assert ( ! locked )

        synchronized ( processedLinks )
        {
            links.findAll { processedLinks.add( it )}
        }
    }


    @Requires({ link && referrer })
    @Ensures({ link in brokenLinks.keySet() })
    void addBrokenLink ( String link, String referrer )
    {
        assert ( ! locked )

        synchronized ( brokenLinks )
        {
            brokenLinks[ link ] << referrer
        }
    }
}
