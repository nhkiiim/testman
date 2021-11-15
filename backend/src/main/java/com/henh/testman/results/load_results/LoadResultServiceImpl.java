package com.henh.testman.results.load_results;

import com.henh.testman.common.config.AsyncConfig;
import com.henh.testman.common.errors.FailLoadTestException;
import com.henh.testman.common.errors.NotFoundException;
import com.henh.testman.histories.History;
import com.henh.testman.histories.HistoryRepository;
import com.henh.testman.results.load_results.request.LoadInsertReq;
import com.henh.testman.tabs.Tab;
import com.henh.testman.tabs.TabRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;

@Service
public class LoadResultServiceImpl implements LoadResultService {

    private static final Logger logger = LoggerFactory.getLogger(LoadResultServiceImpl.class);

    private final LoadResultRepository loadResultRepository;

    private final HistoryRepository historyRepository;

    private final TabRepository tabRepository;
    
    @Resource
    private LoadTestAsync loadTestAsync;

    @Resource
    private AsyncConfig asyncConfig;

    @Autowired
    public LoadResultServiceImpl(LoadResultRepository loadResultRepository, HistoryRepository historyRepository, TabRepository tabRepository) {
        this.loadResultRepository = loadResultRepository;
        this.historyRepository = historyRepository;
        this.tabRepository = tabRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Optional<LoadResult> insertLoad(LoadInsertReq loadInsertReq) {
        try {
            if (asyncConfig.isTaskExecute()) {
                Tab tab = tabRepository.findById(loadInsertReq.getTabSeq())
                        .orElseThrow(() -> new NotFoundException("could not found tab"));
                tab.updateByLoad(loadInsertReq);
                tabRepository.save(tab);

                Future<LoadResult> future = loadTestAsync.work(loadInsertReq);

                historyRepository.save(new History(loadInsertReq));

                return Optional.of(future.get());
            } else {
                logger.debug("Exceeded number of threads");
                throw new TaskRejectedException("스레드 개수 초과");
            }
        } catch (ExecutionException | InterruptedException e) {
            logger.warn("Thread operation failed");
            throw new FailLoadTestException("LOAD TEST 실패 : " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LoadResult> selectLoad(Long seq) {
        checkNotNull(seq, "seq must be provided");
        return loadResultRepository.findById(seq);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoadResult> selectLoadByTabSeq(Long tabSeq) {
        checkNotNull(tabSeq, "tabSeq must be provided");
        return loadResultRepository.findByTabSeq(tabSeq);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteLoad(Long tabSeq) {
        checkNotNull(tabSeq, "tabSeq must be provided");

        List<LoadResult> list = loadResultRepository.findByTabSeq(tabSeq);
        loadResultRepository.deleteAll(list);

        return list.size();
    }

}
