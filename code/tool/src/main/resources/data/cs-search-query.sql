SELECT
        pi.PAYMENTINTERCHANGEKEY,
        pi.FILENAME,
        pi.TRANSPORTTIME,
        pi.CONTROLSUMPSRIDX,
        pi.NUMBEROFTRANSACTIONSPSRIDX,
        pi.STATUS,
        pi.ORIGORIGALIASIDX,
        pi.GROUPSTATUSIDX,
        pi.CHANNELUUIDIDX,
        pi.GPPTYPEIDX,
        pi.ORIGTYPEIDX,
        pi.GPPISSUERIDX,
        pi.VERSION,

        pin.PAYMENTINSTRUCTIONKEY,
        pin.WHENMODIFIED,
        pin.VERSION,
        pin.PAYMENTINFOSTATUSIDX,
        pin.STATUS,
        pin.INSTRUCTIONREFERENCEIDX,
        pin.ORIGINATINGPARTYACCOUNTIDX,
        pin.NUMBEROFTRANSACTIONSPSRIDX,
        pin.INSTRUCTEDDAYIDX,
        pin.APPROVALCOUNTCURRENTIDX,
        pin.APPROVALCOUNTREQUESTEDIDX,
        pin.PRIORITYIDX,
        pin.USERGROUPIDIDX,
        pin.INTERCHANGEKEY,

        pt.PAYMENTTRANSACTIONKEY,
        pt.STATUS,
        pt.PAYMENTSTATUSIDX,
        pt.CREDITPARTYNAMEIDX,
        pt.CREDITPARTYACCOUNTIDTYPEIDX,
        pt.CREDITPARTYACCOUNTIDX,
        pt.REMITTANCE1CREDITPARTYREFIDX,
        pt.INSTRUCTEDAGENTIDIDX,
        pt.INSTRUCTEDAMOUNTIDX,
        pt.INCOMINGINSTRUCTIONKEY

        -- o.OPICKEY
        -- o.STATUS

        -- pt.INSTRUCTEDCURRENCY,
        -- pt.CREDITPARTYACCOUNTCURRENCY,
        -- pt.CREDITPARTYADDRESSLINE2
        -- pt.CREDITPARTYADDRESSLINE1,
        -- pt.CREDITPARTYPOSTALCODE,
        -- pt.CREDITPARTYSTATE,
        -- pt.CREDITPARTYCOUNTRY,
        -- pt.CREDITPARTYBUILDINGNUMBER,
        -- pt.CREDITPARTYSTREETNAME,

        -- pin.ORIGINATINGPARTYACCOUNTCURRENCY,
        -- pin.ORIGINATINGPARTYACCOUNG -- IBAN
        -- pin.CATEGORYPURPOSE,
        -- pin.CATEGORYPURPOSEPRIORITYCODE

        FROM
            PAYMENTINTERCHANGE pi
            , PAYMENTINSTRUCTION pin
            , PAYMENTTRANSACTION pt
            -- , OPIC o -- causes full table access because of cardinality, Interchange-1, Opic-1, Instruction-3, Transaction-5
            -- , EXCHANGECONDITION ec -- causes full table access - same as above?
        WHERE

        -- Join interchange/instruction/transaction/opic
            pi.PAYMENTINTERCHANGEKEY=pin.INTERCHANGEKEY
        AND pin.PAYMENTINSTRUCTIONKEY=pt.INCOMINGINSTRUCTIONKEY
        -- AND o.OPICKEY=pt.OPICKEY, -- see above
        -- AND ec.EXCHANGECONDITIONKEY = pi.CLEARINGCONDITIONKEY -- see above

        -- Interchange criteria
        -- AND pi.paymentinterchangekey <> 1 -- not relevant
        AND pi.USERGROUPIDX = ? -- values: 'KBC', 'Raffizen', 'Pireus', cardinality high, -- index needed here will cause a range scan
        AND pi.BANKGROUPID = 'Credit Suisse Group' -- need an index, although the cardinality is too high and will probably do a range scan or table access
        AND pi.FILENAME like '%A%'
        AND pi.TRANSPORTTIME
                BETWEEN TO_DATE('15/05/2014 8:30:25', 'DD/MM/YYYY HH:MI:SS')
                AND TO_DATE('15/05/2016 8:30:25', 'DD/MM/YYYY HH:MI:SS')
        AND pi.CHANNELUUIDIDX like'%A%'
        AND pi.ORIGTYPEIDX = 'ORIGTYPEIDX'
        AND pi.ORIGISSUERIDX = 'ORIGISSUERIDX'
        AND pi.ORIGORIGALIASIDX like '%ORIG%'
        AND pi.GPPTYPEIDX = 'GPPTYPEIDX'
        AND pi.GPPISSUERIDX = 'GPPISSUERIDX'
        AND pi.INCOMING='T'
        AND pi.GROUPSTATUSIDX = 'AXE'
        AND pi.STATUS = 'Processed'

        -- Instruction criteria
        -- AND pin.paymentInstructionKey <> 1 -- not relevant
        AND pin.PRIORITYIDX = 'Immediate'
        AND pin.SETTLEMENTAMOUNTIDX BETWEEN 0 AND 10000000000000
        AND pin.INSTRUCTEDDAYIDX
                BETWEEN TO_DATE('15/05/2014 8:30:25', 'DD/MM/YYYY HH:MI:SS')
                AND TO_DATE('15/05/2016 8:30:25', 'DD/MM/YYYY HH:MI:SS')
        AND pin.INSTRUCTIONREFERENCEIDX like '%A%'
        AND pin.ORIGINATINGPARTYACCOUNTIDX in ('ORIGINATINGPARTYACCOUNTIDX')
        AND pin.PAYMENTINFOSTATUSIDX = 'XVYI'
        AND pin.STATUS = 'Processed'
        AND pin.APPROVALCOUNTCURRENTIDX = '5555'
        AND pin.APPROVALCOUNTREQUESTEDIDX = '6666'

        -- Transaction criteria
        -- AND pt.PAYMENTTRANSACTIONKEY<> 1 -- not relevant
        AND pt.FIRSTINSTRUCTINGAGENTTRNIDIDX like '%Swift%'
        AND pt.ENDTOENDIDIDX like '%A%'
        AND pt.SETTLEMENTAMOUNTIDX BETWEEN 0 AND 1000000000000
        AND pt.CREDITPARTYAGENTNAMEIDX like '%A%'
        AND pt.ULTIMATECREDITPARTYNAMEIDX like '%A%'
        AND pt.CREDITPARTYACCOUNTIDX like '%A%'
        AND pt.REMITTANCE1CREDITPARTYREFIDX like '%A%'
        AND pt.PAYMENTSTATUSIDX = 'OK'
        AND pt.STATUS = 'Processed'

        -- Opic criteria
        -- AND o.STATUS = 'Processed'

        -- Exchange dcondition criteria - none?

        -- ORDER BY pi.PAYMENTINTERCHANGEKEY ASC -- Causes several orders of magnitude difference
            -- , pin.PAYMENTINSTRUCTIONKEY ASC -- Causes several orders of magnitude difference
            -- , pt.PAYMENTTRANSACTIONKEY ASC; -- Causes several orders of magnitude difference