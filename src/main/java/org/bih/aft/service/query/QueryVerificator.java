package org.bih.aft.service.query;

import org.bih.aft.controller.dao.AqlWithParams;

interface QueryVerificator {

    void verify(AqlWithParams aqlQuery);

}
