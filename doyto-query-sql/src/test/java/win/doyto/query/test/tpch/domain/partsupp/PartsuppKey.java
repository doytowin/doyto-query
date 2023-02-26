package win.doyto.query.test.tpch.domain.partsupp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import win.doyto.query.annotation.ForeignKey;
import win.doyto.query.core.CompositeId;
import win.doyto.query.entity.Persistable;
import win.doyto.query.test.tpch.domain.part.PartEntity;
import win.doyto.query.test.tpch.domain.supplier.SupplierEntity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import javax.persistence.Id;

/**
 * PartsuppKey
 *
 * @author f0rb on 2023/2/24
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartsuppKey implements CompositeId, Persistable<PartsuppKey> {
    @Id
    @ForeignKey(entity = PartEntity.class, field = "p_partkey")
    private Integer ps_partkey;

    @Id
    @ForeignKey(entity = SupplierEntity.class, field = "s_suppkey")
    private Integer ps_suppkey;

    @Override
    public List<Serializable> getKeys() {
        return Arrays.asList(ps_partkey, ps_suppkey);
    }

    @JsonIgnore
    @Override
    public PartsuppKey getId() {
        return new PartsuppKey(ps_partkey, ps_suppkey);
    }

    @Override
    public void setId(PartsuppKey partsuppKey) {
        this.ps_partkey = partsuppKey.ps_partkey;
        this.ps_suppkey = partsuppKey.ps_suppkey;
    }
}
