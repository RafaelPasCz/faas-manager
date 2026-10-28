/* Arquivo para gerar a tabela Function para o banco de dados */

package com.portfolio.faasmanager.model;

import com.portfolio.faasmanager.model.Device;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "tb_function")
public class Function{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "function_url", nullable = false, length = 100)
    private String functionUrl;

    //eu posso implantar funções com o mesmo nome, porem implementações diferentes em vários devices
    //aqui da pra adicionar uma anotação opcional para informar qual é o estilo de implantação
    @Column(name = "implementation_model", nullable = true, length = 100)
    private String implementationModel;


    //chave estrangeira para o device
    //um device pode ter várias funções
    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Device device;

    public Function() {
    }

    public Function(String name, String implementationModel, Device device, String functionUrl) {
        this.name = name;
        this.implementationModel = implementationModel;
        this.device = device;
        this.functionUrl = functionUrl;
    }

    public String getFunctionUrl(){
        return functionUrl;
    }

    public Long getId() {
        return id; 
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) { 
        this.name = name;
    }
    
    public String getImplementationModel() { 
        return implementationModel;
    }
    public void setImplementationModel(String implementationModel) { 
        this.implementationModel = implementationModel;
    }
    
    public Device getDevice() { 
        return device;
    }

    public void setDevice(Device device) {
        this.device = device; 
    }

    public void setFunctionUrl(String newFunctionUrl){
        this.functionUrl = newFunctionUrl;
    }
}