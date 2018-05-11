

K = Indexed key
I = Indexed value
i = Indexed by configuration.  Typical Elements or Terms should be indexed.
P = Processing value (in memory)
p = Processing value that may or may not be present
S = Stored value (if present)
s = Stored per configuration
. = Stored in other elements
X = Directly inherited.  See the sub element.

DOCUMENT
    - doc.id    : KPS : numeric - system unique
    - uri       :  pS : uri - globally unique.  there is a one to one relationship with id within a system
    - type      : IPS : enumerable type
    - TERM*     :  P. : document level terms
    - BLOCK*    :  P. : sub blocks
    
BLOCK
    - BLOCKINFO : X   : immutable block info
    - raw       : iPs : raw data
    - TERM*     :  Ps : block level terms
    - Result?   : iPs : Result for the block
    - Attrib*   : iPs : Name/Value attributes for the block

BLOCKINFO
    - BlockType : IPS : type
    - block.id  : KPS : numeric - document unique
    - owner.id  : iPs : doc or block id.
    - start     : iPs : starting offset in document--inclusive.  Or UNKNOWN (negative number)
    - end       : iPs : ending offset in document--exclusive.  Or UNKNOWN (negative number)

TERM
    - text      : iPs : string text.  This may be altered from the original.  Alter may be persisted in the original document.
    - start     : iPs : istarting offset in block (or document)--inclusive.  Or UNKNOWN (negative number)
    - end       : iPs : ending offset in block (or document)--exclusive.  Or UNKNOWN (negative number)
    - Element?  : X   : an element - the term processed and decorated
    
ELEMENT
    - ElementType :  Ps : type
    - tag         : KPs : string tag text
    - text        : IPs : processed text.  This is not the same as altered.   It may be stored, but not as part of the original.
    - owner.id    : iPs : doc or block id.
        

    