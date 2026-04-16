package resumematcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StopWords {
    public static final Set<String> WORDS = new HashSet<>(Arrays.asList(
            // Articles & conjunctions
            "a","an","the","and","or","but","if","then","else","nor","yet","so",
            // Prepositions
            "for","to","of","in","on","at","by","with","from","into","over","under",
            "as","about","after","before","during","between","through","above","below",
            "up","down","out","off","along","among","around","against","without","within",
            "per","via","re","vs",
            // Pronouns
            "it","its","he","she","they","them","his","her","their","you","your","we",
            "our","i","me","my","mine","us","who","whom","which","that","this","these",
            "those","what","whatever","whoever",
            // Common verbs (generic - not tech-specific)
            "is","am","are","was","were","be","been","being","have","has","had","do",
            "does","did","will","would","shall","should","can","could","may","might",
            "must","need","use","used","using","work","works","worked","working","make",
            "made","making","get","gets","got","getting","provide","provides","provided",
            "seek","seeks","seeking","looked","looking","look","require","requires",
            "required","requiring","understand","understanding","understands","understood",
            "ensure","ensures","ensuring","include","includes","including","included",
            "involve","involves","involved","join","collaborate","assist","support",
            "communicate","maintain","manage","manage","coordinate","contribute",
            "develop","implement","design","build","create","deliver","deploy",
            "help","ensure","achieve","drive","lead","own","define","execute",
            // Common adjectives (generic - not describing tech skills)
            "strong","good","great","excellent","extensive","solid","broad","deep",
            "high","low","new","old","large","small","big","key","main","major",
            "full","fast","best","better","various","multiple","different","similar",
            "specific","general","global","local","current","prior","previous","next",
            "additional","further","related","relevant","preferred","required","optional",
            "proven","demonstrated","clear","effective","efficient","able","capable",
            "passionate","motivated","dedicated","collaborative","proactive","innovative",
            // Generic job-description filler
            "role","position","team","company","organization","environment","opportunity",
            "candidate","applicant","individual","person","professional","expert","member",
            "join","hire","hiring","looking","seeking","responsibilities","responsibility",
            "requirement","requirements","qualification","qualifications","skills","skill",
            "experience","experiences","background","knowledge","understanding","ability",
            "abilities","communication","problem","solving","analytical","detail","oriented",
            "years","year","month","months","day","days","time","plus","least","least",
            "more","most","very","also","well","other","some","any","all","both","each",
            "such","only","own","same","than","too","just","even","never","always","often",
            "usually","typically","generally","primarily","mainly","especially","particularly",
            // Common adverbs
            "not","no","yes","here","there","when","where","why","how","again","further",
            "once","ago","already","still","yet","soon","now","then","today","daily",
            // Numbers as words
            "one","two","three","four","five","six","seven","eight","nine","ten",
            // Resume / JD specific filler
            "etc","eg","ie","via","including","including","preferred","ideally",
            "plus","bonus","nice","degree","field","fields","area","areas",
            "minimum","maximum","least","level","levels","including","across"
    ));
}
