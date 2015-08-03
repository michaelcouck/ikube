package ikube.search;

/**
 * This class holds the query and parameters for the search on the transactions.?
 *
 * @author Michael Couck
 * @version 01.00
 * @since 06-07-2015
 */
public interface GppQuery {

    String[] USER_GROUP_IDS = {
            "KBC",
            "Raffizen",
            "Credit Agricole",
            "Pireus",
            "Aareal Bank",
            "Aargauische Kanton",
            "AB SEB bankas, V",
            "Abanka Vipa, Lj",
            "ABC Banking Co",
            "ABC Internati",
            "ABC Islamic",
    };

    String INTER_INSTRU_TRANS_OPIC_EXCHAN_QUERY = "SELECT \n" +
            "pi.PAYMENTINTERCHANGEKEY, \n" +
            "pi.FILENAME, \n" +
            "pi.TRANSPORTTIME, \n" +
            "pi.CONTROLSUMPSRIDX, \n" +
            "pi.NUMBEROFTRANSACTIONSPSRIDX, \n" +
            "pi.STATUS, \n" +
            "pi.ORIGORIGALIASIDX, \n" +
            "pi.GROUPSTATUSIDX, \n" +
            "pi.CHANNELUUIDIDX, \n" +
            "pi.GPPTYPEIDX, \n" +
            "pi.ORIGTYPEIDX, \n" +
            "pi.GPPISSUERIDX, \n" +
            "pi.VERSION, \n" +
            " \n" +
            "pin.PAYMENTINSTRUCTIONKEY, \n" +
            "pin.WHENMODIFIED, \n" +
            "pin.VERSION, \n" +
            "pin.PAYMENTINFOSTATUSIDX, \n" +
            "pin.STATUS, \n" +
            "pin.INSTRUCTIONREFERENCEIDX, \n" +
            "pin.ORIGINATINGPARTYACCOUNTIDX, \n" +
            "pin.NUMBEROFTRANSACTIONSPSRIDX, \n" +
            "pin.INSTRUCTEDDAYIDX, \n" +
            "pin.APPROVALCOUNTCURRENTIDX, \n" +
            "pin.APPROVALCOUNTREQUESTEDIDX, \n" +
            "pin.PRIORITYIDX, \n" +
            "pin.USERGROUPIDIDX, \n" +
            "pin.INTERCHANGEKEY, \n" +
            " \n" +
            "pt.PAYMENTTRANSACTIONKEY, \n" +
            "pt.STATUS, \n" +
            "pt.PAYMENTSTATUSIDX, \n" +
            "pt.CREDITPARTYNAMEIDX, \n" +
            "pt.CREDITPARTYACCOUNTIDTYPEIDX, \n" +
            "pt.CREDITPARTYACCOUNTIDX, \n" +
            "pt.REMITTANCE1CREDITPARTYREFIDX, \n" +
            "pt.INSTRUCTEDAGENTIDIDX, \n" +
            "pt.INSTRUCTEDAMOUNTIDX, \n" +
            "pt.INCOMINGINSTRUCTIONKEY \n" +
            "-- , a.DOMESTICACCOUNTNUMBER \n" +
            " \n" +
            "-- o.OPICKEY \n" +
            "-- o.STATUS \n" +
            " \n" +
            "-- pt.INSTRUCTEDCURRENCY, \n" +
            "-- pt.CREDITPARTYACCOUNTCURRENCY, \n" +
            "-- pt.CREDITPARTYADDRESSLINE2 \n" +
            "-- pt.CREDITPARTYADDRESSLINE1, \n" +
            "-- pt.CREDITPARTYPOSTALCODE, \n" +
            "-- pt.CREDITPARTYSTATE, \n" +
            "-- pt.CREDITPARTYCOUNTRY, \n" +
            "-- pt.CREDITPARTYBUILDINGNUMBER, \n" +
            "-- pt.CREDITPARTYSTREETNAME, \n" +
            " \n" +
            "-- pin.ORIGINATINGPARTYACCOUNTCURRENCY, \n" +
            "-- pin.ORIGINATINGPARTYACCOUNG -- IBAN \n" +
            "-- pin.CATEGORYPURPOSE, \n" +
            "-- pin.CATEGORYPURPOSEPRIORITYCODE \n" +
            " \n" +
            "FROM \n" +
            "PAYMENTINTERCHANGE pi \n" +
            ", PAYMENTINSTRUCTION pin \n" +
            ", PAYMENTTRANSACTION pt \n" +
            ", OPIC o \n -- causes full table access because of cardinality, Interchange-1, Opic-1, Instruction-3, Transaction-5 \n" +
            ", EXCHANGECONDITION ec \n -- causes full table access - same as above? \n" +
            "-- , ACCOUNT a \n" +
            "WHERE \n" +
            " \n" +
            "-- Join interchange/instruction/transaction/opic \n" +
            "pi.PAYMENTINTERCHANGEKEY = pin.INTERCHANGEKEY \n" +
            "AND pin.PAYMENTINSTRUCTIONKEY = pt.INCOMINGINSTRUCTIONKEY \n" +
            "AND o.OPICKEY=pt.OPICKEY \n -- see above \n" +
            "AND ec.EXCHANGECONDITIONKEY = pi.CLEARINGCONDITIONKEY \n -- see above \n" +
            "-- AND pin.ORIGINATINGPARTYACCOUNTIDX = a.DOMESTICACCOUNTNUMBER " +
            " \n" +
            "-- Interchange criteria \n" +
            "-- AND pi.paymentinterchangekey <> 1 -- not relevant \n" +
            "AND pi.USERGROUPIDX = :USERGROUPIDX \n -- values: 'KBC', 'Raffizen', 'Pireus', cardinality high, -- index needed here will cause a range scan \n" +
            "AND pi.BANKGROUPID = 'Credit Suisse Group' \n -- need an index, although the cardinality is too high and will probably do a range scan or table access \n" +
            "AND pi.FILENAME like '%A%' \n" +
            "AND pi.TRANSPORTTIME \n" +
            "BETWEEN TO_DATE('15/05/2014 8:30:25', 'DD/MM/YYYY HH:MI:SS') \n" +
            "AND TO_DATE('15/05/2016 8:30:25', 'DD/MM/YYYY HH:MI:SS') \n" +
            "AND pi.CHANNELUUIDIDX like'%A%' \n" +
            "AND pi.ORIGTYPEIDX = 'ORIGTYPEIDX' \n" +
            "AND pi.ORIGISSUERIDX = 'ORIGISSUERIDX' \n" +
            "AND pi.ORIGORIGALIASIDX like '%ORIG%' \n" +
            "AND pi.GPPTYPEIDX = 'GPPTYPEIDX' \n" +
            "AND pi.GPPISSUERIDX = 'GPPISSUERIDX' \n" +
            "AND pi.INCOMING='T' \n" +
            "AND pi.GROUPSTATUSIDX = 'AXE' \n" +
            "AND pi.STATUS = 'Processed' \n" +
            " \n" +
            "-- Instruction criteria \n" +
            "-- AND pin.paymentInstructionKey <> 1 -- not relevant \n" +
            "AND pin.PRIORITYIDX = 'Immediate' \n" +
            "AND pin.SETTLEMENTAMOUNTIDX BETWEEN 0 AND 10000000000000 \n" +
            "AND pin.INSTRUCTEDDAYIDX \n" +
            "BETWEEN TO_DATE('15/05/2014 8:30:25', 'DD/MM/YYYY HH:MI:SS') \n" +
            "AND TO_DATE('15/05/2016 8:30:25', 'DD/MM/YYYY HH:MI:SS') \n" +
            "AND pin.INSTRUCTIONREFERENCEIDX like '%A%' \n" +
            "AND pin.ORIGINATINGPARTYACCOUNTIDX in ('ORIGINATINGPARTYACCOUNTIDX') \n" +
            "AND pin.PAYMENTINFOSTATUSIDX = 'XVYI' \n" +
            "AND pin.STATUS = 'Processed' \n" +
            "AND pin.APPROVALCOUNTCURRENTIDX = '5555' \n" +
            "AND pin.APPROVALCOUNTREQUESTEDIDX = '6666' \n" +
            " \n" +
            "-- Transaction criteria \n" +
            "-- AND pt.PAYMENTTRANSACTIONKEY<> 1 -- not relevant \n" +
            "AND pt.FIRSTINSTRUCTINGAGENTTRNIDIDX like '%Swift%' \n" +
            "AND pt.ENDTOENDIDIDX like '%A%' \n" +
            "AND pt.SETTLEMENTAMOUNTIDX BETWEEN 0 AND 1000000000000 \n" +
            "AND pt.CREDITPARTYAGENTNAMEIDX like '%A%' \n" +
            "AND pt.ULTIMATECREDITPARTYNAMEIDX like '%A%' \n" +
            "AND pt.CREDITPARTYACCOUNTIDX like '%A%' \n" +
            "AND pt.REMITTANCE1CREDITPARTYREFIDX like '%A%' \n" +
            "AND pt.PAYMENTSTATUSIDX = 'OK' \n" +
            "AND pt.STATUS = 'Processed' \n" +
            " \n" +
            "-- Opic criteria \n" +
            "-- AND o.STATUS = 'Processed' \n" +
            " \n" +
            "-- Exchange condition criteria - none? \n" +
            " \n" +
            "-- ORDER BY pi.PAYMENTINTERCHANGEKEY ASC -- Causes several orders of magnitude difference \n" +
            "-- , pin.PAYMENTINSTRUCTIONKEY ASC -- Causes several orders of magnitude difference \n" +
            "-- , pt.PAYMENTTRANSACTIONKEY ASC; -- Causes several orders of magnitude difference \n";

}